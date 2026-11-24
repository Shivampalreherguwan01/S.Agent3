package com.sagent.app

import android.content.Context
import com.google.firebase.database.*
import org.webrtc.*
import java.nio.ByteBuffer

class WebRTCClient(
    private val context: Context,
    private val roomCode: String,
    private val isController: Boolean,
    private val eglBase: EglBase,
    private val videoView: SurfaceViewRenderer? = null,
    private val onCommandReceived: ((String) -> Unit)? = null
) {
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null
    private var videoCapturer: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private val database = FirebaseDatabase.getInstance().reference.child("sessions").child(roomCode)

    init {
        initPeerConnectionFactory()
        videoView?.init(eglBase.eglBaseContext, null)
    }

    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
    }

    fun startConnection() {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                val type = if (isController) "controller_candidates" else "source_candidates"
                database.child(type).push().setValue(hashMapOf("sdpMid" to candidate.sdpMid, "sdpMLineIndex" to candidate.sdpMLineIndex, "candidate" to candidate.sdp))
            }
            override fun onDataChannel(channel: DataChannel) { setupDataChannel(channel) }
            override fun onAddStream(stream: MediaStream) {
                if (isController && videoView != null && stream.videoTracks.isNotEmpty()) {
                    stream.videoTracks[0].addSink(videoView)
                }
            }
            override fun onTrack(receiver: RtpReceiver) {
                val track = receiver.track()
                if (isController && videoView != null && track is VideoTrack) {
                    track.addSink(videoView)
                }
            }
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onRenegotiationNeeded() {}
        })

        if (isController) {
            // Controller initiates connection
            val init = DataChannel.Init()
            dataChannel = peerConnection?.createDataChannel("control", init)
            setupDataChannel(dataChannel!!)
            
            // Listen for Source's Answer
            listenForRemoteSdp("answer", "sdp")
            listenForCandidates("source_candidates")
            createOffer()
        } else {
            // Source waits for Offer
            listenForRemoteSdp("offer", "sdp")
            listenForCandidates("controller_candidates")
        }
    }

    private fun createOffer() {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        database.child("offer").setValue(hashMapOf("sdp" to sdp.description))
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    private fun listenForRemoteSdp(nodeName: String, type: String) {
        database.child(nodeName).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sdpStr = snapshot.child("sdp").getValue(String::class.java) ?: return
                val sdp = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdpStr)
                
                peerConnection?.setRemoteDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        if (nodeName == "offer") {
                            // Source creates Answer
                            createAnswer()
                        }
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sdp)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun createAnswer() {
        val constraints = MediaConstraints()
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        database.child("answer").setValue(hashMapOf("sdp" to sdp.description))
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    private fun listenForCandidates(nodeName: String) {
        database.child(nodeName).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val sdpMid = snapshot.child("sdpMid").getValue(String::class.java) ?: return
                val sdpMLineIndex = snapshot.child("sdpMLineIndex").getValue(Int::class.java) ?: return
                val candidateStr = snapshot.child("candidate").getValue(String::class.java) ?: return
                peerConnection?.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, candidateStr))
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupDataChannel(channel: DataChannel) {
        dataChannel = channel
        val dataChannelInit = DataChannel.Init()
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {}
            override fun onStateChange(state: DataChannel.State) {}
            override fun onMessage(buffer: DataChannel.Buffer) {
                val data = ByteArray(buffer.data.remaining())
                buffer.data.get(data)
                val msg = String(data)
                onCommandReceived?.invoke(msg)
            }
        })
    }

    fun sendCommand(command: String) {
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(command.toByteArray()), false)
        dataChannel?.send(buffer)
    }

    // Source: Start Screen Sharing
    fun startScreenCapture(mediaProjection: android.media.projection.MediaProjection) {
        val videoCapturerAndroid = ScreenCapturerAndroid(mediaProjection, object : MediaProjection.Callback() {})
        videoCapturer = videoCapturerAndroid
        
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoSource = peerConnectionFactory.createVideoSource(false)
        videoCapturerAndroid.initialize(surfaceTextureHelper, context, videoSource!!.capturerObserver)
        
        val metrics = context.resources.displayMetrics
        videoCapturerAndroid.startCapture(metrics.widthPixels, metrics.heightPixels, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("VIDEO_TRACK", videoSource)
        localVideoTrack?.addSink(null) // No local preview needed for source
        
        peerConnection?.addTrack(localVideoTrack, listOf("stream1"))
    }
}
