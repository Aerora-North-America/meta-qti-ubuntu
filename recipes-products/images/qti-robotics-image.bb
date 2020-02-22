inherit uimage extrausers

#require ${META_QTI_BSP_IMAGE_PATH}/include/mdm-bootimg.inc
#DEPENDS += " mkbootimg-native "

#require ${META_QTI_BSP_IMAGE_PATH}/include/mdm-ota-target-image-ubi.inc
#require ${META_QTI_BSP_IMAGE_PATH}/include/mdm-ota-target-image-ext4.inc

#MULTILIBRE_ALLOW_REP =. "/usr/include/python2.7/*|${base_bindir}|${base_sbindir}|${bindir}|${sbindir}|${libexecdir}|${sysconfdir}|${nonarch_base_libdir}/udev|/lib/modules/[^/]*/modules.*|"

EXTRA_USERS_PARAMS = "usermod -P oelinux123 root;"
EXTRA_USERS_PARAMS += "usermod -g 3003 _apt;"

do_populate_lic_deploy[noexec] = "1"

DEPENDS += "ubuntu-base"

CORE_IMAGE_BASE_INSTALL = " \
            edk2 \
            kernel-modules \
            adbd \
            binder \
            usb-composition \
            systemd-machine-units \
            ab-slot-util \
            abctl \
            yavta \
            post-boot \
            adsprpc \
            qmi-framework \
            "
#Install packages for wlan
CORE_IMAGE_BASE_INSTALL += " \
            qcacld32-ll \
            cnss-daemon \
            cld80211-lib \
            wlan-conf \
            qmi-framework-vendor \
            "

UBUNTU_TAR_FILE="${EXTERNAL_TOOLCHAIN}/ubuntu-base-18.04.2-base-arm64.tar.gz"

do_ubuntu_rootfs(){
    tar -xf ${UBUNTU_TAR_FILE} --exclude=dev -C ${IMAGE_ROOTFS}
    install -m 0751 -d ${IMAGE_ROOTFS}/dev
    install -m 0777 -d ${IMAGE_ROOTFS}/tmp
    chown -R root:root ${IMAGE_ROOTFS}/bin/su 
    chmod a+s ${IMAGE_ROOTFS}/bin/su 
    #add overlay fs & firmware
    mkdir -p ${IMAGE_ROOTFS}/overlay
    mkdir -p ${IMAGE_ROOTFS}/overlay/etc
    mkdir -p ${IMAGE_ROOTFS}/overlay/.etc-work
    mkdir -p ${IMAGE_ROOTFS}/overlay/data
    mkdir -p ${IMAGE_ROOTFS}/overlay/.data-work
    mkdir -p ${IMAGE_ROOTFS}/firmware
    mkdir -p ${IMAGE_ROOTFS}/lib/firmware
    ln -sf /firmware/image ${IMAGE_ROOTFS}/lib/firmware/updates
    ln -sf /bin/bash   ${IMAGE_ROOTFS}/bin/sh
#   replace the cpufreq governor ondemand with schedutil
    rm -rf ${IMAGE_ROOTFS}/etc/systemd/system/multi-user.target.wants/ondemand.service
#   ---- design to avoid do_rootfs status error ----
#    mv ${IMAGE_ROOTFS}/var/lib/dpkg/status ${IMAGE_ROOTFS}/var/lib/dpkg/status-ubuntu 
#    touch ${IMAGE_ROOTFS}/var/lib/dpkg/status
#
#   ---- fix error : unknown group 'messagebus' in statoverride file ----
#    rm ${IMAGE_ROOTFS}/var/lib/dpkg/statoverride
#    touch ${IMAGE_ROOTFS}/var/lib/dpkg/statoverride
#   ----------------------------------------------------------------------
#   ---- fix error : do_rootfs: Preinstall for package xxxx failed ----
    rm -rf ${IMAGE_ROOTFS}/var/lib/dpkg/info/*.postinst   
    rm -rf ${IMAGE_ROOTFS}/var/lib/dpkg/info/*.preinst   
#   ---- fix user conflicts ----
# 
#   ----------------------------------------------------------------------
    ln -sf /lib/systemd/system/adsprpcd.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/adsprpcd.service
    ln -sf /lib/systemd/system/adsprpcd_rootpd.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/adsprpcd_rootpd.service
    ln -sf /lib/systemd/system/adsprpcd_audiopd.service \
               ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/adsprpcd_audiopd.service
    ln -sf /lib/systemd/system/adsprpcd_sensorspd.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/adsprpcd_sensorspd.service
    ln -sf /lib/systemd/system/cdsprpcd.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/cdsprpcd.service
    ln -sf /lib/systemd/system/adsprpcd.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/adsprpcd.service
    ln -sf /lib/systemd/system/mdsprpcd.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/mdsprpcd.service
    ln -sf /lib/systemd/system/cdsp.service \
                ${IMAGE_ROOTFS}/lib/systemd/system/multi-user.target.wants/cdsp.service
}

do_deb_pre() {
    do_ubuntu_rootfs
}

do_fs_post() {
    #fix adbd launch command
    sed -i "s@start-stop-daemon -S -b -a /sbin/adbd@start-stop-daemon -S -b --exec /sbin/adbd@g" ${IMAGE_ROOTFS}/etc/launch_adbd

    #fix apt status version error
    sed -i "s@git-r@0-1@g" ${IMAGE_ROOTFS}/var/lib/dpkg/status
    sed -i "s@>= git@>= 0@g" ${IMAGE_ROOTFS}/var/lib/dpkg/status

#   ---- fix mesa/adreno file list conflicts ----
    if [ -e ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list ]; then
        sed -i '/usr\/include\/KHR/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/KHR\/khrplatform.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/EGL\/egl.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/EGL\/eglext.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/EGL\/eglplatform.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES2\/gl2.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES2\/gl2ext.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES2\/gl2platform.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES3\/gl3.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES3\/gl31.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES3\/gl32.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
        sed -i '/usr\/include\/GLES3\/gl3platform.h/d' ${IMAGE_ROOTFS}/var/lib/dpkg/info/adreno.list
    fi
}

#----------------------------------------------------------
#---- to record 4 useful Yocto process timing ----
DEB_PREPROCESS_COMMANDS = " do_deb_pre "
#DEB_POSTPROCESS_COMMANDS = " do_deb_post "
#ROOTFS_PREPROCESS_COMMAND += "do_fs_pre; "
ROOTFS_POSTPROCESS_COMMAND += "do_fs_post; "
#----------------------------------------------------------

#Install packages for audio
CORE_IMAGE_BASE_INSTALL += " \
            audiohal \
            audiodlkm \
            init-audio \
            ss-services \
            qmi \
            qmi-framework \
            qmi-framework-vendor \
            tinyalsa \
"

#addtask do_pm before do_rootfs
#addtask do_rec_pm after do_image_qa before do_image_complete
