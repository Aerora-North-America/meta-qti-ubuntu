# Copyright (c) 2021, The Linux Foundation. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#     * Neither the name of The Linux Foundation nor the names of its
#       contributors may be used to endorse or promote products derived
#       from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
# ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
# BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
# BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Changes from Qualcomm Innovation Center are provided under the following license:

# Copyright (c) 2022 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear

# The majority of populate_sdk is located in populate_sdk_base
# which is inherited by populate_sdk_ext. So inheriting
# populate_sdk_ext also helps to run populate_sdk task.

inherit populate_sdk_ext

python copy_buildsystem_append() {
    # Create src directory in extensible SDK to copy the project sources
    bb.utils.mkdirhier(baseoutpath + '/src')
    # Enable the use of WORKSPACE variable on an extensible SDK
    with open(baseoutpath + '/conf/bblayers.conf', 'a') as f:
        f.write('WORKSPACE = "$' + '{TOPDIR}/src"\n')
        f.write('WORKSPACEROOT = "$' + '{TOPDIR}/"\n')
    #Copy HY11 prebuilt tar.gz to extensible SDK
    src_prebuilt_hy11 = os.path.abspath(d.getVar('TOPDIR') + '/../prebuilt_HY11')
    dest_prebuilt_hy11 = os.path.join(baseoutpath,'prebuilt_HY11')
    bb.note("src_prebuilt_hy11 is %s" % src_prebuilt_hy11)
    if os.path.exists(src_prebuilt_hy11):
        shutil.copytree(src_prebuilt_hy11,dest_prebuilt_hy11)

    src_prebuilt_hy22 = os.path.abspath(d.getVar('TOPDIR') + '/../prebuilt_HY22')
    dest_prebuilt_hy22 = os.path.join(baseoutpath,'prebuilt_HY22')
    if os.path.exists(src_prebuilt_hy22):
        shutil.copytree(src_prebuilt_hy22,dest_prebuilt_hy22)

    #set prebuilt tar.gz path when esdk generation
    with open(baseoutpath + '/conf/local.conf', 'a') as f:
        f.write('\nPREBUILT_SRC_DIR = "%s"\n' % '${TOPDIR}/prebuilt_HY11 ${TOPDIR}/prebuilt_HY22')
}

# To include protoc compiler in SDK
TOOLCHAIN_HOST_TASK_append = " nativesdk-protobuf-compiler "

# Add nativesdk-llvm-arm-toolchain in SDK to run on SDKMACHINE
TOOLCHAIN_HOST_TASK_append = " nativesdk-llvm-arm-toolchain"

# To include camera-metadata in SDK
TOOLCHAIN_TARGET_TASK_append = " camera-metadata-dev"

# To include libhardware in SDK
TOOLCHAIN_TARGET_TASK_append = " libhardware-dev"

# To include kernel headers in SDK
TOOLCHAIN_TARGET_TASK_append = " linux-msm-headers-dev"

# To include kernel sources in SDK to build kernel modules
TOOLCHAIN_TARGET_TASK_append = "  ath6kl-utils-staticdev"

#To include glibc-sdk in SDK to support sdllvm
TOOLCHAIN_TARGET_TASK_append = "  glibc-sdk"

#To include linux-libc-headers in SDK to support sdllvm
TOOLCHAIN_TARGET_TASK_append = "  linux-libc-headers"

POPULATE_SDK_POST_TARGET_COMMAND_append = " install_alternative_link;"

fakeroot install_alternative_link() {
    cd ${SDK_OUTPUT}${SDKTARGETSYSROOT}
    touch alternative_install.list
    find . -name "*.postinst" | xargs -i grep -r "update-alternatives --install" {} | xargs -i echo {} > alternative_install.list

    install_list_file="alternative_install.list"

    while IFS= read -r line; do
        link=$(echo "$line" | awk '{print $3}')
        target=$(echo "$line" | awk '{print $5}' |sed 's,//,/,g')
        file_name=$(basename "$link")
        dir_name=$(dirname "$link")

        if [ ! -d '.'${dir_name} ];then
            mkdir -p '.'${dir_name}
        fi
        ln --relative -s '.'${target} '.'${link}
    done < "$install_list_file"
    rm alternative_install.list
}
