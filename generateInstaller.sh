#!/bin/bash -xe
#
# DESCRIPTION
#	This script uses the variable generated the scripts that ran before this and copies things required
#   for the installer into correct directory.

# Source build.config since we need variables defined in it.

WORKSPACE_CYGPATH=$(cygpath --unix ${WORKSPACE})

source ${WORKSPACE_CYGPATH}/build.config
APP_DIR=${WORKSPACE_CYGPATH}/app
SHAREPOINTONLINEPATH=${APP_DIR}/SecureCollaboration_SPOnLineApp-*.zip
SHAREPOINTONPREMISEPATH=${APP_DIR}/SecureCollaboration_SPOnPremiseApp-*.zip
SHAREPOINTONLINEREPOSITORYPATH=${NLEXTERNALDIR2}/sharepoint-online-oauth-app/1.0.0.0/SecureCollaboration_SPOnlineRepositoryApp.app
INSTALLER_DIR=${WORKSPACE_CYGPATH}/installer
BUILD_DIR=${WORKSPACE_CYGPATH}/staging
KMS_BUILD_DIR=${WORKSPACE_CYGPATH}/staging
DOCS_DIR=${WORKSPACE_CYGPATH}/docs
DIST_DIR=${INSTALLER_DIR}/dist
DIST_EXTERNAL_DIR=${DIST_DIR}/external
DIST_RMS_DIR=${DIST_DIR}/rms
ODRM_LIB_DIR=${WORKSPACE_CYGPATH}/projects/rms/lib
ODRM_LIB_LICENSE=${ODRM_LIB_DIR}/license
ODRM_PLATFORM_JKS=${ODRM_LIB_DIR}/platform

INSTALL_BUILD_DIR=${BUILD_DIR}/installer
WINDOWS_DIR=${INSTALL_BUILD_DIR}/windows
LINUX_DIR=${INSTALL_BUILD_DIR}/linux
DIST_WINDOWS=${WINDOWS_DIR}/dist
DIST_LINUX=${LINUX_DIR}/dist
ENGINE_WINDOWS=${WINDOWS_DIR}/engine
ENGINE_LINUX=${LINUX_DIR}/engine
WINDOWS_EXTERNAL=${DIST_WINDOWS}/external
WINDOWS_RMS=${DIST_WINDOWS}/rms
WINDOWS_RMS_LICENSE=${WINDOWS_RMS}/license
LINUX_EXTERNAL=${DIST_LINUX}/external
LINUX_RMS=${DIST_LINUX}/rms
LINUX_RMS_LICENSE=${LINUX_RMS}/license
WINDOWS_SHAREPOINT=${DIST_WINDOWS}/sharepoint
LINUX_SHAREPOINT=${DIST_LINUX}/sharepoint
WINDOWS_INSTALLER_NAME=RightsMgmtSrv-Win-${VERSION_RMS_STR}-${BUILD_NUMBER}.zip
LINUX_INSTALLER_NAME=RightsMgmtSrv-Lin-${VERSION_RMS_STR}-${BUILD_NUMBER}.tar.gz
WINDOWS_BIN=${WINDOWS_DIR}/bin
LINUX_BIN=${LINUX_DIR}/bin
WINDOWS_JAVAPC=${DIST_WINDOWS}/javapc
LINUX_JAVAPC=${DIST_LINUX}/javapc
WINDOWS_DIST_CERT=${DIST_WINDOWS}/conf/cert
LINUX_DIST_CERT=${DIST_LINUX}/conf/cert
WINDOWS_RMS_LIB=${WINDOWS_RMS}/lib
LINUX_RMS_LIB=${LINUX_RMS}/lib
LINUX_RMS_TOOLS=${LINUX_RMS}/tools
#cleanup
rm -rf ${INSTALL_BUILD_DIR:?} && echo "INFO: Deleted existing installer directory: ${INSTALL_BUILD_DIR}"
rm -f ${BUILD_DIR}/RMS-Windows-*.zip && echo "INFO: Deleted existing windows installer archive"
rm -f ${BUILD_DIR}/RMS-Linux-*.zip && echo "INFO: Deleted existing linux installer archive"

#Make directories
mkdir -p ${INSTALL_BUILD_DIR}
mkdir -p ${WINDOWS_DIR}
mkdir -p ${LINUX_DIR}

cp -r ${INSTALLER_DIR}/bin ${WINDOWS_DIR}
cp -r ${INSTALLER_DIR}/cookbooks ${WINDOWS_DIR}
cp -r ${INSTALLER_DIR}/dist ${WINDOWS_DIR}
cp ${INSTALLER_DIR}/install.bat ${WINDOWS_DIR}
cp ${INSTALLER_DIR}/uninstall.bat ${WINDOWS_DIR}
cp ${INSTALLER_DIR}/setup.json ${WINDOWS_DIR}

cp -r ${INSTALLER_DIR}/bin ${LINUX_DIR}
cp -r ${INSTALLER_DIR}/cookbooks ${LINUX_DIR}
cp -r ${INSTALLER_DIR}/dist ${LINUX_DIR}
cp ${INSTALLER_DIR}/install.sh ${LINUX_DIR}
cp ${INSTALLER_DIR}/uninstall.sh ${LINUX_DIR}
cp ${INSTALLER_DIR}/setup.json ${LINUX_DIR}

mkdir -p ${DIST_WINDOWS}
mkdir -p ${DIST_LINUX}
mkdir -p ${ENGINE_WINDOWS}
mkdir -p ${ENGINE_LINUX}
mkdir -p ${WINDOWS_EXTERNAL}
mkdir -p ${WINDOWS_RMS}
mkdir -p ${LINUX_EXTERNAL}
mkdir -p ${LINUX_RMS}
mkdir -p ${WINDOWS_RMS_LICENSE}
mkdir -p ${LINUX_RMS_LICENSE}
mkdir -p ${WINDOWS_SHAREPOINT}
mkdir -p ${LINUX_SHAREPOINT}
mkdir -p ${WINDOWS_JAVAPC}
mkdir -p ${LINUX_JAVAPC}
mkdir -p ${WINDOWS_RMS_LIB}
mkdir -p ${LINUX_RMS_LIB}

#Copy artifacts to engine folder
#Assuming ${NLEXTERNALDIR2} has the external folder
cp ${NLEXTERNALDIR2}/7za/7za_4.57/7za.exe ${ENGINE_WINDOWS}/ || exit -1
cp ${NLEXTERNALDIR2}/chef/chef-client/12.4/engine_linux.zip ${ENGINE_LINUX}/ || exit -1
cp ${NLEXTERNALDIR2}/chef/chef-client/12.4/gems_linux.zip ${ENGINE_LINUX}/ || exit -1
cp ${NLEXTERNALDIR2}/chef/chef-client/12.4/engine_winx.zip ${ENGINE_WINDOWS}/ || exit -1
cp ${NLEXTERNALDIR2}/chef/chef-client/12.4/gems_winx.zip ${ENGINE_WINDOWS}/ || exit -1
cd ${ENGINE_WINDOWS}/ || exit -1
./7za.exe x engine_winx.zip -y > /dev/null || exit -1
./7za.exe x gems_winx.zip -y > /dev/null || exit -1
#Delete long filepath
rm -rf ${ENGINE_WINDOWS}/chef/embedded/lib/ruby/gems/2.0.0/gems/chef-12.4.1-universal-mingw32/spec/data/cookbooks/openldap/files/default/remotedir/subdir_with_no_file_just_a_subsubdir || exit -1
rm -f engine_winx.zip || exit -1
rm -f gems_winx.zip || exit -1

#Copy docs into Documentation folder
mkdir -p ${WINDOWS_DIR}/docs
mkdir -p ${LINUX_DIR}/docs
cp ${DOCS_DIR}/RightsManagementServer_8_4_1_AdminGuide.pdf ${WINDOWS_DIR}/docs || exit -1
cp ${DOCS_DIR}/RightsManagementServer_8_4_1_AdminGuide.pdf ${LINUX_DIR}/docs || exit -1
cp ${DOCS_DIR}/RightsManagementServer_8_4_1_ReleaseNotes.pdf ${WINDOWS_DIR}/docs || exit -1
cp ${DOCS_DIR}/RightsManagementServer_8_4_1_ReleaseNotes.pdf ${LINUX_DIR}/docs || exit -1

#Copy common files to dist external folder
cp ${NLEXTERNALDIR2}/tomcat-zip/apache-tomcat-8.5.35/x64/apache-tomcat-8.5.35-windows-x64.zip ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/IpToCountryDB/5.6.7/IpToCountry.csv ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/tomcat-zip/apache-tomcat-8.5.35/x64/apache-tomcat-8.5.35-windows-x64.zip ${LINUX_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/IpToCountryDB/5.6.7/IpToCountry.csv ${LINUX_EXTERNAL}/ || exit -1

#Copy Windows specific file to temporary dist external
cp ${NLEXTERNALDIR2}/jre/1.8_60/windows/x64/jre-8u60-windows-x64.tar.gz ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/vcredist/2012/vcredist_x64_30679.exe ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/vcredist/2013/vcredist_x64_40784.exe ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/vcredist/2015/vcredist_x64_48145.exe ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/vcredist/2010/vcredist_x64_14632.exe ${WINDOWS_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/mysql/5.7.11/mysql-5.7.11-winx64.zip ${WINDOWS_EXTERNAL}/ || exit -1

#Copy linux specific files to temporary dist external
cp ${NLEXTERNALDIR2}/jre/1.8_60/linux/x64/jre-8u60-linux-x64.tar.gz ${LINUX_EXTERNAL}/ || exit -1
cp ${NLEXTERNALDIR2}/mysql/5.7.11/mysql-5.7.11-linux-glibc2.5-x86_64.tar.gz ${LINUX_EXTERNAL}/ || exit -1

cp ${BUILD_DIR}/RMS.war ${WINDOWS_RMS}/ || exit -1
cp ${BUILD_DIR}/RMS.war ${LINUX_RMS}/ || exit -1
cp ${KMS_BUILD_DIR}/KMS.war ${WINDOWS_RMS}/ || exit -1
cp ${KMS_BUILD_DIR}/KMS.war ${LINUX_RMS}/ || exit -1
cp ${ODRM_LIB_LICENSE}/license.jar ${WINDOWS_RMS_LICENSE} || exit -1
cp ${ODRM_LIB_LICENSE}/license.jar ${LINUX_RMS_LICENSE} || exit -1
cp ${BUILD_DIR}/commons-codec-1.10.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/commons-codec-1.10.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/RMSUtil.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/RMSUtil.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/RMEncryptionUtil.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/RMEncryptionUtil.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/sqljdbc41.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/sqljdbc41.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/ojdbc7.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/ojdbc7.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/mysql-connector-java-5.1.38.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/mysql-connector-java-5.1.38.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/KMS.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/KMS.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/KMS_xmlbeans.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/KMS_xmlbeans.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/commons-io-2.4.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/commons-io-2.4.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/slf4j-api-1.7.13.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/slf4j-api-1.7.13.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/slf4j-log4j12-1.7.13.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/slf4j-log4j12-1.7.13.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/log4j-1.2.17.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/log4j-1.2.17.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/org.restlet-2.3.5.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/org.restlet-2.3.5.jar ${LINUX_RMS_LIB} || exit -1
cp ${BUILD_DIR}/org.restlet.ext.jaxb-2.3.5.jar ${WINDOWS_RMS_LIB} || exit -1
cp ${BUILD_DIR}/org.restlet.ext.jaxb-2.3.5.jar ${LINUX_RMS_LIB} || exit -1

#Copy files to sharepoint folder
cp ${SHAREPOINTONLINEPATH} ${WINDOWS_SHAREPOINT}/ || exit -1
cp ${SHAREPOINTONPREMISEPATH} ${WINDOWS_SHAREPOINT}/ || exit -1
cp ${SHAREPOINTONLINEREPOSITORYPATH} ${WINDOWS_SHAREPOINT}/ || exit -1
cp ${SHAREPOINTONLINEPATH} ${LINUX_SHAREPOINT}/ || exit -1
cp ${SHAREPOINTONPREMISEPATH} ${LINUX_SHAREPOINT}/ || exit -1
cp ${SHAREPOINTONLINEREPOSITORYPATH} ${LINUX_SHAREPOINT}/ || exit -1

#Copy embeddedpdp files
rm -rf ${BUILD_DIR:?}/Policy_Controller_Java || exit -1
WIN_BUILD_DIR=$(cygpath --windows ${BUILD_DIR})
ESCAPED_WIN_BUILD_DIR=$(echo ${WIN_BUILD_DIR} | sed 's@\\@\\\\@g')
${NLEXTERNALDIR2}/7za/7za_4.57/7za.exe x -o${ESCAPED_WIN_BUILD_DIR} ${XLIB_DESTINY_BUILD_ARTIFACTS_BIN_ZIP_FILE} Policy_Controller_Java/dist/embeddedpdp/* -r || exit -1
cd ${BUILD_DIR}/Policy_Controller_Java/dist/embeddedpdp/ ; tar cf - . | (cd ${WINDOWS_JAVAPC}/ ; tar xf -) || exit -1
cd ${BUILD_DIR}/Policy_Controller_Java/dist/embeddedpdp/ ; tar cf - . | (cd ${LINUX_JAVAPC}/ ; tar xf -) || exit -1

rm -rf ${BUILD_DIR:?}/bin || exit -1
${NLEXTERNALDIR2}/7za/7za_4.57/7za.exe x -o${ESCAPED_WIN_BUILD_DIR} ${XLIB_FATE_BUILD_ARTIFACTS_BIN_ZIP_FILE} bin/java/KeyManagementService.jar || exit -1
cp ${BUILD_DIR}/bin/java/KeyManagementService.jar ${WINDOWS_JAVAPC} || exit -1
cp ${BUILD_DIR}/bin/java/KeyManagementService.jar ${LINUX_JAVAPC} || exit -1

mkdir -p ${WINDOWS_DIST_CERT} || exit -1
mkdir -p ${LINUX_DIST_CERT} || exit -1
cp ${ODRM_PLATFORM_JKS}/temp_agent-keystore.jks ${WINDOWS_DIST_CERT} || exit -1
cp ${ODRM_PLATFORM_JKS}/temp_agent-keystore.jks ${LINUX_DIST_CERT} || exit -1

# Convert from unix-type to dos-type for windows setup.json
perl -pi -e 's@\n@\r\n@g' ${WINDOWS_DIR}/setup.json || exit -1
rm -f ${WINDOWS_DIR}/setup.json.bak || exit -1
#Remove shell files from windows installer
rm -f ${WINDOWS_BIN}/setup.sh || exit -1
rm -f ${WINDOWS_BIN}/uninst/uninstall.sh || exit -1
#Remove batch files from linux installer
rm -f ${LINUX_BIN}/setup.bat || exit -1
rm -f ${LINUX_BIN}/uninst/uninstall.bat || exit -1

chmod +x ${LINUX_DIR}/install.sh || exit -1
chmod +x ${LINUX_DIR}/uninstall.sh || exit -1
chmod +x ${LINUX_BIN}/setup.sh || exit -1
chmod +x ${LINUX_BIN}/uninst/uninstall.sh || exit -1
chmod +x ${LINUX_RMS_TOOLS}/crypt.sh || exit -1
chmod +x ${LINUX_RMS_TOOLS}/keymanagement.sh || exit -1

cd ${WINDOWS_DIR}/ || exit -1
${NLEXTERNALDIR2}/7za/7za_4.57/7za.exe a ${WINDOWS_INSTALLER_NAME} * || exit -1
mv ${WINDOWS_INSTALLER_NAME} ${BUILD_DIR}/ || exit -1

tar -zcf ${BUILD_DIR}/${LINUX_INSTALLER_NAME} --directory=${LINUX_DIR}/ . || exit -1

cd ${DIST_WINDOWS}/ || exit -1
${NLEXTERNALDIR2}/7za/7za_4.57/7za.exe a javapc-windows.zip javapc || exit -1
mv javapc-windows.zip ${BUILD_DIR}/ || exit -1

cd ${DIST_LINUX}/ || exit -1
tar -zcvf javapc-linux.tar javapc || exit -1
mv javapc-linux.tar ${BUILD_DIR}/ || exit -1

cd ${BUILD_DIR} || exit -1

rm -rf ${INSTALL_BUILD_DIR:?} && echo "INFO: Deleted existing installer directory: ${INSTALL_BUILD_DIR}" || exit -1

## EOF ##
