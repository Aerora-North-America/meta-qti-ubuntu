EXTRA_OECONF_append_qrb5165 = " --libdir=${libdir}/${UBUN_TARGET_SYS}"
FILES_${PN}_append_qrb5165 = " ${libdir}/${UBUN_TARGET_SYS}/*"
RDEPENDS_${PN} += " gstreamer1.0-plugins-base-audio "
