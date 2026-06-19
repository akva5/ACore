package akv5.acore.libs;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.awt.Color;

public class Colors {

    public static String set(String from) {
        from = from.replaceAll("&#([a-fA-F0-9]{6})", "#$1");

        Pattern gradientPattern = Pattern.compile("\\{#([a-fA-F0-9]{6})>}(.*?)\\{#([a-fA-F0-9]{6})<}");
        Matcher gradientMatcher = gradientPattern.matcher(from);

        while (gradientMatcher.find()) {
            try {
                String startColorHex = gradientMatcher.group(1);
                String text = gradientMatcher.group(2);
                String endColorHex = gradientMatcher.group(3);

                String gradientText = createGradient(text, startColorHex, endColorHex);
                from = from.replace(gradientMatcher.group(0), gradientText);
                gradientMatcher = gradientPattern.matcher(from);
            } catch (Exception e) {
                break;
            }
        }



        Pattern pattern = Pattern.compile("\\{#([a-fA-F0-9]{6})}|#([a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(from);

        while (matcher.find()) {
            try {
                String hexCode;
                String fullMatch = matcher.group(0);

                if (fullMatch.startsWith("{")) {
                    hexCode = matcher.group(1);
                } else {
                    hexCode = matcher.group(2);
                }

                StringBuilder colorCode = new StringBuilder("&x");
                for (char c : hexCode.toCharArray()) {
                    colorCode.append("&").append(c);
                }

                from = from.replace(fullMatch, colorCode.toString());
                matcher = pattern.matcher(from);
            } catch (Exception e) {
                break;
            }
        }

        return ChatColor.translateAlternateColorCodes('&', from);
    }

    public static String clear(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));
    }

    private static String createGradient(String text, String startHex, String endHex) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            Color startColor = Color.decode("#" + startHex);
            Color endColor = Color.decode("#" + endHex);

            StringBuilder gradient = new StringBuilder();
            int length = text.length();

            for (int i = 0; i < length; i++) {
                float ratio = (float) i / Math.max(1, (length - 1));
                Color interpolatedColor = interpolateColor(startColor, endColor, ratio);

                String hexColor = String.format("%06X", (0xFFFFFF & interpolatedColor.getRGB()));
                gradient.append("&x");

                for (char c : hexColor.toCharArray()) {
                    gradient.append("&").append(c);
                }

                gradient.append(text.charAt(i));
            }

            return gradient.toString();
        } catch (Exception e) {
            return text;
        }
    }

    private static Color interpolateColor(Color start, Color end, float ratio) {
        int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
        int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
        int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new Color(red, green, blue);
    }

    public static String strip(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String stripped = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));

        stripped = stripped.replaceAll("\\{#[a-fA-F0-9]{6}\\}", "");

        stripped = stripped.replaceAll("\\{#[a-fA-F0-9]{6}>\\}", "");

        stripped = stripped.replaceAll("\\{#[a-fA-F0-9]{6}<\\}", "");

        stripped = stripped.replaceAll("#[a-fA-F0-9]{6}", "");

        return stripped;
    }

    public static List<String> set(List<String> source) {
        return source.stream().map(Colors::set).collect(Collectors.toList());
    }
}