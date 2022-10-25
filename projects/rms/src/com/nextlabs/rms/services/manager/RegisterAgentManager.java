package com.nextlabs.rms.services.manager;


import noNamespace.*;

import java.math.BigInteger;
import java.util.*;

public class RegisterAgentManager {
    private static final Map<com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits, TimeUnits.Enum> timeUnitMap;
    static {
        Map<com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits, TimeUnits.Enum> map = new HashMap<com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits, TimeUnits.Enum>();
        map.put(com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits.DAYS, TimeUnits.DAYS);
        map.put(com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits.HOURS, TimeUnits.HOURS);
        map.put(com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits.MINUTES, TimeUnits.MINUTES);
        map.put(com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits.SECONDS, TimeUnits.SECONDS);
        map.put(com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits.MILLISECONDS, TimeUnits.MILLISECONDS);
        timeUnitMap = Collections.unmodifiableMap(map);
    }
    private static final Map<com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO, ActionTypeDTO.Enum> actionTypeMap;
    static {
        Map<com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO, ActionTypeDTO.Enum> map = new HashMap<com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO, ActionTypeDTO.Enum>();
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.OPEN, ActionTypeDTO.OPEN);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.EDIT, ActionTypeDTO.EDIT);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.DELETE, ActionTypeDTO.DELETE);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.CHANGE_ATTRIBUTES, ActionTypeDTO.CHANGE_ATTRIBUTES);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.CHANGE_SECURITY, ActionTypeDTO.CHANGE_SECURITY);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.PASTE, ActionTypeDTO.PASTE);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.MOVE, ActionTypeDTO.MOVE);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.RENAME, ActionTypeDTO.RENAME);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.COPY, ActionTypeDTO.COPY);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.EMBED, ActionTypeDTO.EMBED);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.EMAIL, ActionTypeDTO.EMAIL);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.IM, ActionTypeDTO.IM);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.PRINT, ActionTypeDTO.PRINT);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.STOP_AGENT, ActionTypeDTO.STOP_AGENT);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.START_AGENT, ActionTypeDTO.START_AGENT);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.AGENT_USER_LOGIN, ActionTypeDTO.AGENT_USER_LOGIN);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.AGENT_USER_LOGOUT, ActionTypeDTO.AGENT_USER_LOGOUT);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.ACCESS_AGENT_CONFIG, ActionTypeDTO.ACCESS_AGENT_CONFIG);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.ACCESS_AGENT_LOGS, ActionTypeDTO.ACCESS_AGENT_LOGS);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.ACCESS_AGENT_BINARIES, ActionTypeDTO.ACCESS_AGENT_BINARIES);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.ACCESS_AGENT_BUNDLE, ActionTypeDTO.ACCESS_AGENT_BUNDLE);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.ABNORMAL_AGENT_SHUTDOWN, ActionTypeDTO.ABNORMAL_AGENT_SHUTDOWN);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.INVALID_BUNDLE, ActionTypeDTO.INVALID_BUNDLE);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.BUNDLE_RECEIVED, ActionTypeDTO.BUNDLE_RECEIVED);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.EXPORT, ActionTypeDTO.EXPORT);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.ATTACH, ActionTypeDTO.ATTACH);
        map.put(com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO.RUN, ActionTypeDTO.RUN);
        actionTypeMap = Collections.unmodifiableMap(map);
    }

    private static RegisterAgentManager instance = new RegisterAgentManager();

    private RegisterAgentManager(){
    }

    public static RegisterAgentManager getInstance(){
        return instance;
    }

    public AgentStartupConfiguration getRMSAgentStartupConfiguration(com.nextlabs.rms.services.cxf.destiny.services.agent.types.AgentStartupConfiguration ccConfig) {
        AgentStartupConfiguration rmsConfig = AgentStartupConfiguration.Factory.newInstance();

        if (ccConfig == null) {
            rmsConfig.setId(0L);
        } else {
        	  BigInteger ccConfigId = ccConfig.getId();
        	  rmsConfig.setId(ccConfigId.longValue());
        }

        BigInteger ccRegistrationId = ccConfig.getRegistrationId();
        if (ccRegistrationId == null) {
            rmsConfig.setRegistrationId(0L);
        } else {
            rmsConfig.setRegistrationId(ccRegistrationId.longValue());
        }

        rmsConfig.setClassificationProfile(HeartbeatManager.getClassificationProfile());

        AgentProfileDTO rmsAgentProfile = getRMSAgentProfileDTO(ccConfig.getAgentProfile());
        rmsConfig.setAgentProfile(rmsAgentProfile);

        CommProfileDTO rmsCommProfile = getRMSCommProfileDTO(ccConfig.getCommProfile());
        rmsConfig.setCommProfile(rmsCommProfile);
        
        return rmsConfig;
    }

    public AgentProfileDTO getRMSAgentProfileDTO(com.nextlabs.rms.services.cxf.destiny.services.management.types.AgentProfileDTO agentProfileDTO) {
        if (agentProfileDTO == null) {
            return null;
        }

        AgentProfileDTO rmsAgentProfile = AgentProfileDTO.Factory.newInstance();

        rmsAgentProfile.setLogViewingEnabled(agentProfileDTO.isLogViewingEnabled());
        rmsAgentProfile.setTrayIconEnabled(agentProfileDTO.isTrayIconEnabled());
        rmsAgentProfile.setCreatedDate(agentProfileDTO.getCreatedDate().toGregorianCalendar());
        rmsAgentProfile.setModifiedDate(agentProfileDTO.getModifiedDate().toGregorianCalendar());
        rmsAgentProfile.setId(agentProfileDTO.getId());
        rmsAgentProfile.setName(agentProfileDTO.getName());

        return rmsAgentProfile;
    }

    public CommProfileDTO getRMSCommProfileDTO(com.nextlabs.rms.services.cxf.destiny.services.management.types.CommProfileDTO ccInputCommProfile) {
        if (ccInputCommProfile == null) {
            return null;
        }

        CommProfileDTO rmsCommProfile = CommProfileDTO.Factory.newInstance();

        List<com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO> actions = ccInputCommProfile.getCurrentActivityJournalingSettings().getLoggedActivities().getAction();
        rmsCommProfile.setCurrentActivityJournalingSettings(getRMSActivityJournalingSettingsDTO(actions));

        actions = ccInputCommProfile.getCustomActivityJournalingSettings().getLoggedActivities().getAction();
        rmsCommProfile.setCustomActivityJournalingSettings(getRMSActivityJournalingSettingsDTO(actions));

        int heartbeatFrequency = ccInputCommProfile.getHeartBeatFrequency().getTime();
        com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits hbTimeUnit = ccInputCommProfile.getHeartBeatFrequency().getTimeUnit();
        TimeIntervalDTO hbTime = TimeIntervalDTO.Factory.newInstance();
        hbTime.setTime(heartbeatFrequency);
        hbTime.setTimeUnit(timeUnitMap.get(hbTimeUnit));

        rmsCommProfile.setHeartBeatFrequency(hbTime);

        int logFrequency = ccInputCommProfile.getLogFrequency().getTime();
        com.nextlabs.rms.services.cxf.destiny.framework.types.TimeUnits logTimeUnit = ccInputCommProfile.getLogFrequency().getTimeUnit();
        TimeIntervalDTO logTime = TimeIntervalDTO.Factory.newInstance();
        logTime.setTime(logFrequency);
        logTime.setTimeUnit(timeUnitMap.get(logTimeUnit));

        rmsCommProfile.setLogFrequency(logTime);

        rmsCommProfile.setLogLimit(ccInputCommProfile.getLogLimit().intValue());
        rmsCommProfile.setPasswordHash(ccInputCommProfile.getPasswordHash());

        rmsCommProfile.setCreatedDate(ccInputCommProfile.getCreatedDate().toGregorianCalendar());
        rmsCommProfile.setModifiedDate(ccInputCommProfile.getModifiedDate().toGregorianCalendar());
        rmsCommProfile.setId(ccInputCommProfile.getId());
        rmsCommProfile.setName(ccInputCommProfile.getName());

        return rmsCommProfile;
    }

    public ActivityJournalingSettingsDTO getRMSActivityJournalingSettingsDTO(List<com.nextlabs.rms.services.cxf.domain.types.ActionTypeDTO> actions) {
        ActivityJournalingSettingsDTO settings = ActivityJournalingSettingsDTO.Factory.newInstance();
        ActionTypeDTOList actionList = ActionTypeDTOList.Factory.newInstance();

        if (actions == null || actions.size() == 0) {
            actionList.setActionArray(null);
        } else {
            List<ActionTypeDTO.Enum> actionArrayList = new ArrayList<ActionTypeDTO.Enum>();
            for (int i=0; i < actions.size(); ++i) {
                actionArrayList.add(actionTypeMap.get(actions.get(i)));
            }
            ActionTypeDTO.Enum[] actionArray = new ActionTypeDTO.Enum[actionArrayList.size()];
            actionArray = actionArrayList.toArray(actionArray);
            actionList.setActionArray(actionArray);
        }
        settings.setLoggedActivities(actionList);

        return settings;
    }
 }