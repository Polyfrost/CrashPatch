package cc.woverflow.crashpatch.hooks;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModsCheckerPlugin implements IFMLLoadingPlugin {
    private static final JsonParser PARSER = new JsonParser();
    public static final HashMap<String, Triple<File, String, String>> modsMap = new HashMap<>(); //modid : file, version, name

    public ModsCheckerPlugin() {
        File modsFolder = new File(getMcDir(), "mods");
        File[] modFolder = modsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        HashMap<String, ArrayList<Triple<File, String, String>>> dupeMap = new HashMap<>();
        if (modFolder != null) {
            for (File file : modFolder) {
                try {
                    try (ZipFile mod = new ZipFile(file)) {
                        ZipEntry entry = mod.getEntry("mcmod.info");
                        if (entry != null) {
                            try (InputStream inputStream = mod.getInputStream(entry)) {
                                byte[] availableBytes = new byte[inputStream.available()];
                                inputStream.read(availableBytes, 0, inputStream.available());
                                JsonObject modInfo = PARSER.parse(new String(availableBytes)).getAsJsonArray().get(0).getAsJsonObject();
                                if (!modInfo.has("modid") || !modInfo.has("version")) {
                                    continue;
                                }

                                String modid = modInfo.get("modid").getAsString();
                                if (modsMap.containsKey(modid)) {
                                    if (dupeMap.containsKey(modid)) {
                                        dupeMap.get(modid).add(new Triple<>(file, modInfo.get("version").getAsString(), modInfo.has("name") ? modInfo.get("name").getAsString() : modid));
                                    } else {
                                        dupeMap.put(modid, Lists.newArrayList(modsMap.get(modid), new Triple<>(file, modInfo.get("version").getAsString(), modInfo.has("name") ? modInfo.get("name").getAsString() : modid)));
                                    }
                                } else {
                                    modsMap.put(modid, new Triple<>(file, modInfo.get("version").getAsString(), modInfo.has("name") ? modInfo.get("name").getAsString() : modid));
                                }
                            }
                        }
                    }
                } catch (MalformedJsonException | IllegalStateException ignored) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Iterator<ArrayList<Triple<File, String, String>>> iterator = dupeMap.values().iterator();

        while (iterator.hasNext()) {
            try {
                ArrayList<Triple<File, String, String>> next = iterator.next();
                List<Triple<File, String, String>> blank = next.stream().sorted((a, b) -> {
                    if (a != null && b != null) {
                        try {
                            int value = new DefaultArtifactVersion(substringBeforeAny(a.second, "-beta", "-alpha", "-pre", "+beta", "+alpha", "+pre")).compareTo(new DefaultArtifactVersion(substringBeforeAny(b.second, "-beta", "-alpha", "-pre", "+beta", "+alpha", "+pre")));
                            return -value;
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                String[] array = {a.second, b.second};
                                Arrays.sort(array);
                                return array[0].equals(a.second) ? -1 : Objects.equals(a.second, b.second) ? 0 : 1;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return 0;
                            }
                        }
                    }
                    return 0;
                }).collect(Collectors.toList());
                next.clear();
                next.addAll(blank);
                ListIterator<Triple<File, String, String>> otherIterator = next.listIterator();
                int index = 0;
                while (otherIterator.hasNext()) {
                    Triple<File, String, String> remove = otherIterator.next();
                    ++index;
                    if (index != 1) {
                        if (tryDeleting(remove.first)) {
                            otherIterator.remove();
                        } else {
                            doThatPopupThing(modsFolder, "Duplicate mods have been detected! These mods are...\n" +
                                    getStringOf(dupeMap.values()) + "\nPlease removes these mods from your mod folder, which is opened." + ((new File(getMcDir(), "W-OVERFLOW/CrashPatch/SKYCLIENT").exists() || containsAnyKey(ModsCheckerPlugin.modsMap, "skyclientcosmetics", "scc", "skyclientaddons", "skyblockclientupdater", "skyclientupdater", "skyclientcore")) ? " GO TO https://inv.wtf/skyclient FOR MORE INFORMATION." : ""));
                        }
                    }
                }
                if (next.size() <= 1) {
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (!dupeMap.isEmpty()) {
            doThatPopupThing(modsFolder, "Duplicate mods have been detected! These mods are...\n" +
                    getStringOf(dupeMap.values()) + "\nPlease removes these mods from your mod folder, which is opened." + ((new File(getMcDir(), "W-OVERFLOW/CrashPatch/SKYCLIENT").exists() || containsAnyKey(ModsCheckerPlugin.modsMap, "skyclientcosmetics", "scc", "skyclientaddons", "skyblockclientupdater", "skyclientupdater", "skyclientcore")) ? " GO TO https://inv.wtf/skyclient FOR MORE INFORMATION." : ""));
        }
    }

    private static void doThatPopupThing(File modsFolder, String message) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        DesktopManager.open(modsFolder);
        JOptionPane.showMessageDialog(frame, message, "Duplicate Mods Detected!", JOptionPane.ERROR_MESSAGE);
        try {
            Class<?> exitClass = Class.forName("java.lang.Shutdown");
            Method exit = exitClass.getDeclaredMethod("exit", int.class);
            exit.setAccessible(true);
            exit.invoke(null, 0);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SafeVarargs
    private final <A, B> boolean containsAnyKey(HashMap<A, B> hashMap, A... any) {
        for (A thing : any) {
            if (hashMap.containsKey(thing)) return true;
        }
        return false;
    }

    private String getStringOf(Collection<ArrayList<Triple<File, String, String>>> dupes) {
        StringBuilder builder = new StringBuilder();
        for (ArrayList<Triple<File, String, String>> list : dupes) {
            builder.append("\n");
            for (Triple<File, String, String> triple : list) {
                builder.append(" ").append(triple.first.getAbsolutePath());
            }
        }
        return builder.toString().trim();
    }

    private String substringBeforeAny(String string, String... values) {
        String returnString = string;
        for (String value : values) {
            if (returnString.contains(value)) {
                returnString = StringUtils.substringBefore(returnString, value);
            }
        }
        return returnString;
    }

    private boolean tryDeleting(File file) {
        if (!file.delete()) {
            if (!file.delete()) {
                if (!file.delete()) {
                    file.deleteOnExit();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static class Triple<A, B, C> {
        public A first;
        public B second;
        public C third;

        public Triple(A a, B b, C c) {
            first = a;
            second = b;
            third = c;
        }

        @Override
        public String toString() {
            return "Triple{" +
                    "first=" + first +
                    ", second=" + second +
                    ", third=" + third +
                    '}';
        }
    }

    /**
     * Taken from UniversalCraft under LGPLv3
     * https://github.com/EssentialGG/UniversalCraft/blob/master/LICENSE
     */
    private static class DesktopManager {
        private static final boolean isLinux;
        private static final boolean isXdg;
        private static boolean isKde;
        private static boolean isGnome;
        private static final boolean isMac;
        private static final boolean isWindows;

        static {
            String osName;
            try {
                osName = System.getProperty("os.name");
            } catch (SecurityException ignored) {
                osName = null;
            }
            isLinux = osName != null && (osName.startsWith("Linux") || osName.startsWith("LINUX"));
            isMac = osName != null && osName.startsWith("Mac");
            isWindows = osName != null && osName.startsWith("Windows");
            if (isLinux) {
                String xdg = System.getenv("XDG_SESSION_ID");
                isXdg = xdg != null && !xdg.isEmpty();
                String gdm = System.getenv("GDMSESSION");
                if (gdm != null) {
                    String lowercaseGDM = gdm.toLowerCase(Locale.ENGLISH);
                    isGnome = lowercaseGDM.contains("gnome");
                    isKde = lowercaseGDM.contains("kde");
                }
            } else {
                isXdg = false;
                isKde = false;
                isGnome = false;
            }
        }


        public static void open(File file) {
            if (!openDesktop(file)) {
                openSystemSpecific(file.getPath());
            }
        }

        private static boolean openSystemSpecific(String file) {
            return isLinux ? (isXdg ? runCommand("xdg-open \"" + file + '"') : (isKde ? runCommand("kde-open \"" + file + '"') : (isGnome ? runCommand("gnome-open \"" + file + '"') : runCommand("kde-open \"" + file + '"') || runCommand("gnome-open \"" + file + '"')))) : (isMac ? runCommand("open \"" + file + '"') : (isWindows && runCommand("explorer \"" + file + '"')));
        }

        private static boolean openDesktop(File file) {
            boolean worked;
            if (!Desktop.isDesktopSupported()) {
                worked = false;
            } else {
                boolean worked2;
                try {
                    if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                        return false;
                    }

                    Desktop.getDesktop().open(file);
                    worked2 = true;
                } catch (Throwable var4) {
                    worked2 = false;
                }

                worked = worked2;
            }

            return worked;
        }

        private static boolean runCommand(String command) {
            try {
                Process process = Runtime.getRuntime().exec(command);
                return process != null && process.isAlive();
            } catch (IOException var5) {
                return false;
            }
        }
    }

    private static File getMcDir() {
        return new File(System.getProperty("user.dir"));
    }
}
