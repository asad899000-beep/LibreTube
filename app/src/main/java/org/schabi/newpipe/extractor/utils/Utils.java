package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Utils {

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    private static final Pattern M_PATTERN = Pattern.compile("(https?)?://m\\.");
    private static final Pattern WWW_PATTERN = Pattern.compile("(https?)?://www\\.");

    private Utils() {
        // no instance
    }

    public static String encodeUrlUtf8(final String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }

    public static String decodeUrlUtf8(final String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 decoding not supported", e);
        }
    }

    public static String removeNonDigitCharacters(final String string) {
        return string.replaceAll("\\D+", "");
    }

    public static long mixedNumberWordToLong(final String numberWord)
            throws NumberFormatException, ParsingException {
        String multiplier = "";
        try {
            multiplier = Parser.matchGroup("[\\d]+([\\.,][\\d]+)?([KMBkmb])+", numberWord, 2);
        } catch (final ParsingException ignored) {
        }

        final double count = Double.parseDouble(
                Parser.matchGroup1("([\\d]+([\\.,][\\d]+)?)", numberWord).replace(",", ".")
        );

        switch (multiplier.toUpperCase()) {
            case "K":
                return (long) (count * 1000.0d);
            case "M":
                return (long) (count * 1000000.0d);
            case "B":
                return (long) (count * 1.0E9d);
            default:
                return (long) count;
        }
    }

    public static void checkUrl(final String pattern, final String url) throws ParsingException {
        checkUrl(Pattern.compile(pattern), url);
    }

    public static void checkUrl(final Pattern pattern, final String url) throws ParsingException {
        if (isNullOrEmpty(url)) {
            throw new IllegalArgumentException("Url can't be null or empty");
        }
        if (!Parser.isMatch(pattern, url.toLowerCase())) {
            throw new ParsingException("Url doesn't match the pattern");
        }
    }

    public static String replaceHttpWithHttps(final String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }

    public static String getQueryValue(final URL url, final String key) {
        final String query = url.getQuery();
        if (query != null) {
            final String[] pairs = query.split("&");
            for (final String pair : pairs) {
                final String[] split = pair.split("=", 2);
                final String currentKey = decodeUrlUtf8(split[0]);
                if (currentKey.equals(key)) {
                    return decodeUrlUtf8(split[1]);
                }
            }
        }
        return null;
    }

    public static URL stringToURL(final String url) throws MalformedURLException {
        try {
            return new URL(url);
        } catch (final MalformedURLException e) {
            if (Objects.equals(e.getMessage(), "no protocol: " + url)) {
                return new URL("https://" + url);
            }
            throw e;
        }
    }

    public static boolean isHTTP(final URL url) {
        final String protocol = url.getProtocol();
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            return false;
        }
        final boolean isDefaultPort = url.getPort() == url.getDefaultPort();
        final boolean isNoPort = url.getPort() == -1;
        return isNoPort || isDefaultPort;
    }

    public static String removeMAndWWWFromUrl(final String url) {
        if (M_PATTERN.matcher(url).find()) {
            return url.replace("m.", "");
        }
        if (WWW_PATTERN.matcher(url).find()) {
            return url.replace("www.", "");
        }
        return url;
    }

    public static String removeUTF8BOM(final String s) {
        String result = s;
        if (result.startsWith("\uFEFF")) {
            result = result.substring(1);
        }
        if (result.endsWith("\uFEFF")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String getBaseUrl(final String url) throws ParsingException {
        try {
            final URL parsedUrl = stringToURL(url);
            return parsedUrl.getProtocol() + "://" + parsedUrl.getAuthority();
        } catch (final MalformedURLException e) {
            final String message = e.getMessage();
            if (message != null && message.startsWith("unknown protocol: ")) {
                return message.substring("unknown protocol: ".length());
            }
            throw new ParsingException("Could not get base URL from: " + url, e);
        }
    }

    public static String followGoogleRedirectIfNeeded(final String url) {
        try {
            final URL parsedUrl = stringToURL(url);
            if (parsedUrl.getHost().contains("google") && "/url".equals(parsedUrl.getPath())) {
                final String redirectUrl = Parser.matchGroup1("&url=([^&]+)(?:&|$)", url);
                return decodeUrlUtf8(redirectUrl);
            }
        } catch (final Exception ignored) {
        }
        return url;
    }

    public static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <K, V> boolean isNullOrEmpty(final Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isBlank(final String string) {
        return string == null || string.trim().isEmpty();
    }

    public static String join(
            final String delimiter,
            final String keyValueSeparator,
            final Map<? extends CharSequence, ? extends CharSequence> map
    ) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + keyValueSeparator + entry.getValue())
                .collect(Collectors.joining(delimiter));
    }

    public static String nonEmptyAndNullJoin(
            final CharSequence delimiter,
            final String... elements
    ) {
        return Arrays.stream(elements)
                .filter(s -> !isNullOrEmpty(s) && !"null".equals(s))
                .collect(Collectors.joining(delimiter));
    }

    public static String getStringResultFromRegexArray(
            final String text,
            final String[] regexArray
    ) throws Parser.RegexException {
        return getStringResultFromRegexArray(text, regexArray, 0);
    }

    public static String getStringResultFromRegexArray(
            final String text,
            final Pattern[] regexArray
    ) throws Parser.RegexException {
        return getStringResultFromRegexArray(text, regexArray, 0);
    }

    public static String getStringResultFromRegexArray(
            final String text,
            final String[] regexArray,
            final int group
    ) throws Parser.RegexException {
        final Pattern[] patterns = Arrays.stream(regexArray)
                .filter(Objects::nonNull)
                .map(Pattern::compile)
                .toArray(Pattern[]::new);
        return getStringResultFromRegexArray(text, patterns, group);
    }

    public static String getStringResultFromRegexArray(
            final String text,
            final Pattern[] patterns,
            final int group
    ) throws Parser.RegexException {
        for (final Pattern pattern : patterns) {
            try {
                final String result = Parser.matchGroup(pattern, text, group);
                if (result != null) {
                    return result;
                }
            } catch (final Parser.RegexException ignored) {
            }
        }
        throw new Parser.RegexException("None of the regexes matched the text for group " + group);
    }
}
