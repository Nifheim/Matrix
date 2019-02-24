package io.github.beelzebu.matrix.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamUtils {

    public static final List<String> WHITELIST = new ArrayList<>();
    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final Pattern ipPattern = Pattern.compile("((?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]{1,1}[0-2]{1,3})[ ]?[.,-:; ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,3})[ ]?[., ][ ]?(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,3})[ ]?[., ][ ]?(?:[2-6]\\d[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,3}))(?![0-9]))");
    private static final Pattern pattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final Pattern ipPattern2 = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    private static final String[] blockedCCTLD = {"ml", "gs", "nu", "tk"};

    private static boolean validate(String ip) {
        return pattern.matcher(ip).matches();
    }

    private static boolean checkIP(String message) {
        Matcher regexMatcher = ipPattern.matcher(message);

        while (regexMatcher.find()) {
            if (regexMatcher.group().length() != 0 && ipPattern.matcher(message).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean check(String message) {
        message = Normalizer.normalize(message, Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        return checkIP(message);
    }

    public static boolean checkSpam(String message) {
        message = message.toLowerCase();
        for (String whitelisted : WHITELIST) {
            whitelisted = whitelisted.toLowerCase();
            if (message.equalsIgnoreCase(whitelisted)) {
                return false;
            }
            if (message.contains(whitelisted)) {
                return false;
            }
            if (message.matches(whitelisted)) {
                return false;
            }
            Pattern pattern = Pattern.compile(whitelisted, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(0) != null;
            }
            String[] words = message.split(" ");
            if (words.length > 1) {
                for (String w : words) {
                    if (!checkSpam(w)) {
                        return false;
                    }
                }
            }
        }
        if (message.matches(ipPattern2.pattern())) {
            return true;
        }
        if (message.matches(ipPattern.pattern()) ||
                message.contains(",com") || message.contains(".com") || message.contains(" . com") || message.contains("(punto)com") || message.contains("[punto]com") || message.contains("{punto}com") || message.contains("<punto>com") || message.contains("puntocom") || message.contains("(.)") || message.contains(".(com)") || message.contains("ⓒ") ||
                message.contains(" . es") || message.contains("( . ) es") || message.contains("(punto) es") || message.contains("( punto ) es") ||
                message.contains(",net") || message.contains(".net") || message.contains("{punto}net") || message.contains("[punto]net") || message.contains("<punto>net") || message.contains("(punto)net") || message.contains("puntonet") || message.contains("ⓝ") || message.contains("ⓞ") || message.contains("ｃ") || message.contains("ｎ") ||
                message.contains(",org") || message.contains(".org") || message.contains("(punto)org") || message.contains("[punto]org") || message.contains("{punto}org") || message.contains("<punto>org") || message.contains("puntoorg") || message.contains("(,)") || message.contains(",co") ||
                message.contains("[]com") || message.contains("[]net") || message.contains("[]io") || message.contains("[]org") || message.contains(".co") || message.contains("(punto)co") || message.contains("[punto]co") || message.contains("{punto}co") || message.contains("<punto>co") || message.contains("puntoco") || message.contains(",es") || message.contains(".es") || message.contains("(punto)es") || message.contains("[punto]es") || message.contains("{punto}es") || message.contains("<punto>es") || message.contains("puntoes") || message.contains(",fr") || message.contains(".fr") || message.contains("<.> net") || message.contains("<.>net") || message.contains("<.>com") || message.contains("<,>net") || message.contains("<,> net") || message.contains("<.> com") || message.contains("(punto)fr") || message.contains("[punto]fr") || message.contains("{punto}fr") || message.contains("<punto>fr") || message.contains("puntofr") || message.contains(" . fr") || message.contains("( . ) fr") || message.contains("(punto) fr") || message.contains("( punto ) fr") || message.contains("<,>com") || message.contains("<,> com") || message.contains("<>com") || message.contains("<> com") || message.contains("<> net") || message.contains("<>net") || message.contains("*com") || message.contains("*net") || message.contains("*org") || message.contains("* com") || message.contains("* net") || message.contains("* org") || message.contains("(dawt)") || message.contains("{dawt}") || message.contains("punto com") || message.contains("punto org") || message.contains("punto net") || message.contains("{punto}")) {
            return true;
        }
        if (check(message) && message.contains(".")) {
            return true;
        }
        for (String cctld : blockedCCTLD) {
            String tmsg = message.replace(" ", "");
            if (tmsg.contains("." + cctld) || tmsg.contains("," + cctld) || tmsg.contains("(.)" + cctld)) {
                return true;
            }
        }
        return isAdvertising(message);
    }

    private static boolean isAdvertising(String m) {
        return validate(m) || check(m);
    }
}