RDEPENDS_packagegroup-qti-gst_append = " \
      ${@bb.utils.contains("DISTRO_FEATURES", "wayland", "gstreamer1.0-plugins-bad-waylandsink", "", d)} \
      ${@bb.utils.contains("DISTRO_FEATURES", "pulseaudio", "gstreamer1.0-plugins-good-pulse", "", d)} \
      ${@bb.utils.contains("BASEMACHINE", "qrb5165", "gstreamer1.0-plugins-qti-oss-codec2", "", d)} \
    "
