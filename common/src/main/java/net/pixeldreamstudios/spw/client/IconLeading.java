package net.pixeldreamstudios.spw.client;

public final class IconLeading {
    private IconLeading() {}

    public record Split(String icon, String text) {
        public boolean hasIcon() {
            return !icon.isEmpty();
        }
    }

    public static Split split(String raw) {
        int[] span = firstIconSpan(raw);
        if (span[0] < 0) {
            return new Split("", stripSectionCodes(raw).trim());
        }
        String icon = raw.substring(span[0], span[1]);
        String rest = raw.substring(0, span[0]) + raw.substring(span[1]);
        return new Split(icon, stripSectionCodes(rest).replaceFirst("^\\s+", ""));
    }

    private static boolean isIconGlyph(int cp) {
        return (cp >= 0xE000 && cp <= 0xF8FF)
                || (cp >= 0xF900 && cp <= 0xFAFF)
                || (cp >= 0x1CD00 && cp <= 0x1CDFF)
                || (cp >= 0x1FB00 && cp <= 0x1FBFF)
                || (cp >= 0xF0000 && cp <= 0xFFFFD)
                || (cp >= 0x100000 && cp <= 0x10FFFD);
    }

    private static int[] firstIconSpan(String s) {
        int formattingStart = -1;

        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);

            if (cp == '§') {
                if (formattingStart == -1) {
                    formattingStart = i;
                }
                if (i + 1 < s.length()) {
                    int next = s.codePointAt(i + 1);
                    if (next == 'x' || next == 'X') {
                        i += 2;
                        for (int k = 0; k < 6 && i < s.length(); k++) {
                            if (s.charAt(i) == '§' && i + 1 < s.length()) {
                                i += 2;
                            } else {
                                break;
                            }
                        }
                        continue;
                    }
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }

            if (isIconGlyph(cp)) {
                return new int[]{formattingStart >= 0 ? formattingStart : i, i + len};
            }

            formattingStart = -1;
            i += len;
        }
        return new int[]{-1, -1};
    }

    private static String stripSectionCodes(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);

            if (cp == '§') {
                if (i + 1 < s.length()) {
                    int next = s.codePointAt(i + 1);
                    if (next == 'x' || next == 'X') {
                        i += 2;
                        for (int k = 0; k < 6 && i < s.length(); k++) {
                            if (s.charAt(i) == '§' && i + 1 < s.length()) {
                                i += 2;
                            } else {
                                break;
                            }
                        }
                        continue;
                    }
                    i += 2;
                    continue;
                }
                i++;
                continue;
            }
            out.appendCodePoint(cp);
            i += len;
        }
        return out.toString();
    }
}
