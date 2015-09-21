package org.talend.rcp.branding.tos.onboarding;

import org.talend.presentation.onboarding.interfaces.IOnBoardingJsonI18n;
import org.talend.rcp.branding.tos.i18n.Messages;

public class OnBoardingI18n implements IOnBoardingJsonI18n {

    public OnBoardingI18n() {
        // nothing need to do
    }

    @Override
    public String getI18NString(String key) {
        return Messages.getString(key);
    }

}
