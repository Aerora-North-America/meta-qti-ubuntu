RDEPENDS_packagegroup-qti-gst_append = " \
      ${@bb.utils.contains("DISTRO_FEATURES", "wayland", "gstreamer1.0-plugins-bad-waylandsink", "", d)} \
      ${@bb.utils.contains("DISTRO_FEATURES", "pulseaudio", "gstreamer1.0-plugins-good-pulse", "", d)} \
    "
