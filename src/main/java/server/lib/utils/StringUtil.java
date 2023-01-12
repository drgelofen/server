package server.lib.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpStatus;
import server.Application;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public class StringUtil {

    public static UUID getUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (Throwable ignored) {
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    public static String format(String number) {
        if (isEmpty(number)) {
            return number;
        }
        try {
            return number.trim()
                    .replace("١", "1").replace("٢", "2")
                    .replace("٣", "3").replace("٤", "4")
                    .replace("٥", "5").replace("٦", "6")
                    .replace("٧", "7").replace("٨", "8")
                    .replace("٩", "9").replace("٠", "0")
                    .replace("۱", "1").replace("۲", "2")
                    .replace("۳", "3").replace("۴", "4")
                    .replace("۵", "5").replace("۶", "6")
                    .replace("۷", "7").replace("۸", "8")
                    .replace("۹", "9").replace("۰", "0");
        } catch (Throwable ignored) {
            return number;
        }
    }

    public static String formatAlphabet(String input) {
        try {
            return format(input
                    .replace("ك", "ک")
                    .replace("دِ", "د")
                    .replace("زِ", "ز")
                    .replace("بِ", "ب")
                    .replace("ذِ", "ذ")
                    .replace("سِ", "س")
                    .replace("شِ", "ش")
                    .replace("ى", "ی")
                    .replace("ي", "ی"));
        } catch (Throwable ignored) {
            return input;
        }
    }

    public static String formatPrice(Double amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    public static boolean isEmpty(String input) {
        return input == null || input.trim().length() == 0;
    }

    public static String certify(String callBackUrl) {
        if (Application.ENABLE_SSL) {
            if (callBackUrl.toLowerCase().startsWith("http://")) {
                return "https://" + callBackUrl.substring(7);
            }
        }
        return callBackUrl;
    }

    public static String certifyNonSSL(String callbackUrl){
        return "http://" + callbackUrl.substring(7);
    }

    public static String random(int i) {
        return RandomStringUtils.randomNumeric(i);
    }

    public static String randomAlphaNumeric(int i) {
        return RandomStringUtils.randomAlphanumeric(i).toLowerCase();
    }

    public static String randomAlphabets(int i) {
        return RandomStringUtils.randomAlphabetic(i).toLowerCase();
    }

    public static String translate(HttpStatus statusCode) {
        switch (statusCode) {
            case OK:
                return "درخواست شما با موفقیت انجام شد";
            case BAD_REQUEST:
                return "ورودی های ارسالی را تکمیل نمایید، سپس درخواست خود را مجددا ارسال نمایید";
            case BAD_GATEWAY:
                return "درگاه اینترنتی مورد نظر در دسترس نمیباشد، لطفا بعد از چند دقیقه مجددا تلاش کنید";
            case FORBIDDEN:
                return "متاسفانه دسترسی شما به این بخش محدود شده است";
            case EXPECTATION_FAILED:
                return "درخواست مورد نظر به درستی انجام نشد";
            case NOT_FOUND:
                return "بخش مورد نظر یافت نشد، لطفا آدرس انتخابی خود را بررسی کنید";
            case CONFLICT:
                return "درخواست مشابهی با این داده ثبت شده است، لطفا درخواست خود را بازبینی نمایید";
            case LOCKED:
                return "در حال حاضر سیستم پاسخگوی درخواست شما نمیباشد، لطفا بعدا تلاش کنید";
            case UNAUTHORIZED:
                return "شما دسترسی لازم برای ارسال درخواست در این بخش را ندارید";
        }
        return "اشکال در برقراری ارتباط، لطفا مجددا تلاش کنید";
    }

    public static boolean isEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static boolean isUUID(String input) {
        try {
            return UUID.fromString(input) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
