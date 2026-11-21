package com.hanout.identity;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Component;

@Component
public class PhoneValidator {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public boolean isValidTunisianNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(phone, "TN");
            return phoneUtil.isValidNumberForRegion(parsed, "TN");
        } catch (NumberParseException e) {
            return false;
        }
    }

    public String formatE164(String phone) {
        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(phone, "TN");
            return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            return phone;
        }
    }
}
