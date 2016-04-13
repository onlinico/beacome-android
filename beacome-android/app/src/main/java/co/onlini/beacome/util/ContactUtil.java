package co.onlini.beacome.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import co.onlini.beacome.R;
import co.onlini.beacome.model.Contact;
import co.onlini.beacome.model.ContactType;

public class ContactUtil {

    public static ContactType getContactType(Context context, int code) {
        return new ContactType(code, getContactTypeName(context, code));
    }

    public static String getContactTypeName(Context context, int contactType) {
        String value;
        switch (contactType) {
            case 104:
                value = context.getString(R.string.contact_type_phone_company);
                break;
            case 103:
                value = context.getString(R.string.contact_type_phone_work);
                break;
            case 101:
                value = context.getString(R.string.contact_type_phone_mobile);
                break;
            case 102:
                value = context.getString(R.string.contact_type_phone_home);
                break;
            case 105:
                value = context.getString(R.string.contact_type_phone_fax);
                break;
            case 106:
                value = context.getString(R.string.contact_type_phone_other);
                break;
            case 201:
                value = context.getString(R.string.contact_type_email_personal);
                break;
            case 202:
                value = context.getString(R.string.contact_type_email_work);
                break;
            case 203:
                value = context.getString(R.string.contact_type_email_other);
                break;
            case 301:
                value = context.getString(R.string.contact_type_url_website);
                break;
            case 302:
                value = context.getString(R.string.contact_type_url_skype);
                break;
            default:
                value = context.getString(R.string.contact_type_url_other);
        }
        return value;
    }

    public static List<Contact> getPhones(List<Contact> contacts) {
        ArrayList<Contact> phones = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.getContactType() > 99 && contact.getContactType() < 200) {
                phones.add(contact);
            }
        }
        return phones;
    }

    public static List<Contact> getEmails(List<Contact> contacts) {
        ArrayList<Contact> emails = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.getContactType() > 199 && contact.getContactType() < 300) {
                emails.add(contact);
            }
        }
        return emails;
    }

    public static List<Contact> getUrls(List<Contact> contacts) {
        ArrayList<Contact> urls = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.getContactType() > 299) {
                urls.add(contact);
            }
        }
        return urls;
    }
}
