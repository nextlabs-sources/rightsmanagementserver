#!/bin/bash

while [[ $# > 0 ]]
do
key="$1"
case $key in
    -path)
	export KEYSTORE_PATH_PROVIDED=true
	export KEYSTORE_PATH=$2
	shift
    ;;
	-p)
	export KEYSTORE_PASS_PROVIDED=true
	export KEYSTORE_PASS=$2
	shift
    ;;
	-name)
	export KEYRING_NAME_PROVIDED=true
	export KEYRING_NAME=$2
	shift
    ;;
	-tid)
	export TENANT_ID_PROVIDED=true
	export TENANT_ID=$2
	shift
    ;;
	-kmsUrl)
	export KMS_URL_PROVIDED=true
	export KMS_URL=$2
	shift
    ;;
    *)
	if [ $key != -h ]; then echo Invalid Argument; fi;
    ;;
esac
shift
done

if [ "${KEYSTORE_PATH_PROVIDED}" != "true" ]; then
	export KEYSTORE_PATH=''
fi

if [ "${KEYSTORE_PASS_PROVIDED}" != "true" ]; then
	export KEYSTORE_PASS=''
fi

if [ "${KEYRING_NAME_PROVIDED}" != "true" ]; then
	export KEYRING_NAME=''
fi

if [ "${TENANT_ID_PROVIDED}" != "true" ]; then
	export TENANT_ID=''
fi

if [ "${KMS_URL_PROVIDED}" != "true" ]; then
	export KMS_URL=''
fi

"../../../external/jre/bin/java" -cp "KMS.jar:RMSUtil.jar:KMS_xmlbeans.jar:commons-io-2.4.jar:commons-codec-1.10.jar:slf4j-api-1.7.13.jar:slf4j-log4j12-1.7.13.jar:log4j-1.2.17.jar:org.restlet-2.3.5.jar:org.restlet.ext.jaxb-2.3.5.jar" com.nextlabs.kms.importer.DefaultKeyImportUtil -path "${KEYSTORE_PATH}" -p "${KEYSTORE_PASS}" -name "${KEYRING_NAME}" -tid "${TENANT_ID}" -kmsUrl "${KMS_URL}"