package br.nom.penha.bruno.kubeson.common.controller;

import static br.nom.penha.bruno.kubeson.common.controller.UpgradeState.DOWNLOADING;
import static br.nom.penha.bruno.kubeson.common.controller.UpgradeState.UNPACKING;
import static br.nom.penha.bruno.kubeson.common.controller.UpgradeState.UPGRADE_AVAILABLE;
import static br.nom.penha.bruno.kubeson.common.controller.UpgradeState.UPGRADE_ERROR;
import static br.nom.penha.bruno.kubeson.common.controller.UpgradeState.UPGRADE_SUCCESSFUL;
import static br.nom.penha.bruno.kubeson.common.controller.UpgradeState.VALIDATING;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import br.nom.penha.bruno.kubeson.Configuration;
import br.nom.penha.bruno.kubeson.Main;
import br.nom.penha.bruno.kubeson.common.gui.InfoButton;
import br.nom.penha.bruno.kubeson.common.util.ThreadFactory;
import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.ProxySearch.Strategy;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Upgrade {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\D*(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)");

    private static URL GITHUB_RELEASES_URL;

    private static Task downloadWorker;

    private static Release releaseToUpgrade;

    private static UpgradeState upgradeState;

    private static String message;

    private static Proxy proxy;

    private Upgrade() {

    }

    public static void start() {
        ThreadFactory.newThread(() -> {
            try {
                init();
                for (; ; ) {
                    checkForUpgrade();
                    TimeUnit.MILLISECONDS.sleep(Configuration.CHECK_FOR_UPGRADE_WORKER_WAIT_TIME_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.info("Check For Upgrade Thread Interrupted");
            }
            LOGGER.info("Check For Upgrade Thread Completed");
        });
    }

    private static void init() {
        setTrustAllCertificates();
        try {
            GITHUB_RELEASES_URL = new URL(Configuration.GITHUB_RELEASES + "?access_token=" + Configuration.GITHUB_TOKEN);

            // Find http proxy (if any) to be able to upgrade
            ProxySearch proxySearch = new ProxySearch();
            proxySearch.addStrategy(Strategy.OS_DEFAULT);
            proxySearch.addStrategy(Strategy.BROWSER);
            proxySearch.addStrategy(Strategy.ENV_VAR);
            proxySearch.addStrategy(Strategy.JAVA);

            List<Proxy> proxies = proxySearch.getProxySelector().select(GITHUB_RELEASES_URL.toURI());
            if (proxies != null && proxies.size() > 0) {
                proxy = proxies.get(0);
                LOGGER.info("Found {} proxies. Using proxy {}", proxies.size(), proxy);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.error("Failed to create github release url object and proxy check, this should never happen", e);
        }

        upgradeState = UpgradeState.NO_UPGRADE_AVAILABLE;
        message = "";
        downloadWorker = downloadRelease();
    }

    /**
     * Trust all certificates, the signature check of the upgrade package already provides the required security
     */
    private static void setTrustAllCertificates() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            LOGGER.error("Failed to create trust all https certificates, this should never happen", e);
        }
    }

    public static void startDownload() {
        ThreadFactory.newThread(downloadWorker);
    }

    public static UpgradeState getState() {
        return upgradeState;
    }

    public static void setState(UpgradeState state) {
        setState(state, null);
    }

    public static void setState(UpgradeState state, String errorMessage) {
        final String version;
        if (releaseToUpgrade != null) {
            version = releaseToUpgrade.tagName;
        } else {
            version = "";
        }
        switch (state) {
            case UPGRADE_AVAILABLE:
                Platform.runLater(() -> {
                    InfoButton.setYellow();
                    Main.showUpgradeMessage(version);
                });
                break;
            case UPGRADE_ERROR:
                Platform.runLater(InfoButton::setRed);
                break;
            case UPGRADE_SUCCESSFUL:
                Platform.runLater(InfoButton::setBlue);
                break;
        }
        message = String.format(state.getMessage(), version, errorMessage);
        upgradeState = state;
        Platform.runLater(InfoButton::refreshUpgrade);
    }

    public static String getMessage() {
        return message;
    }

    public static Task getDownloadWorker() {
        return downloadWorker;
    }

    private static Task downloadRelease() {
        return new Task() {
            @Override
            protected Object call() {
                if (upgradeState == UPGRADE_AVAILABLE) {
                    setState(DOWNLOADING);
                    try (BufferedInputStream in = new BufferedInputStream(getConnection(new URL(releaseToUpgrade.url)).getInputStream());
                            FileOutputStream fileOutputStream = new FileOutputStream("newapp.zip")) {

                        int buffer = 4096;
                        byte dataBuffer[] = new byte[buffer];
                        int bytesRead;
                        int totalBytes = 0;
                        while ((bytesRead = in.read(dataBuffer, 0, buffer)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                            totalBytes += buffer;
                            super.updateProgress(totalBytes, releaseToUpgrade.size);
                        }

                        setState(VALIDATING);
                        validate("newapp.zip");

                        setState(UNPACKING);
                        unzip("newapp.zip", "newapp", "app");

                        setState(UPGRADE_SUCCESSFUL);

                    } catch (Exception e) {
                        setState(UPGRADE_ERROR, e.getMessage());
                        LOGGER.error("Failed to upgrade to release " + releaseToUpgrade.tagName, e);
                    } finally {
                        try {
                            Files.deleteIfExists(new File("newapp.zip").toPath());
                        } catch (IOException e) {
                            LOGGER.error("Failed to delete file newapp.zip", e);
                        }
                    }
                }

                return null;
            }
        };
    }

    private static void checkForUpgrade() {
        try {
            LOGGER.info("Performing Upgrade Check");

            HttpURLConnection con = (HttpURLConnection) getConnection(GITHUB_RELEASES_URL);
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            if (con.getResponseCode() == 200) {
                JsonNode root = OBJECT_MAPPER.readTree(con.getInputStream());

                if (root.isArray()) {
                    ArrayNode arr = (ArrayNode) root;

                    for (int i = arr.size() - 1; i >= 0; i--) {
                        JsonNode node = arr.get(i);
                        if (!node.get("draft").asBoolean() && !node.get("prerelease").asBoolean()) {
                            String tagName = node.get("tag_name").asText();
                            Version newVersion = new Version(tagName);
                            if (isNewerVersion(newVersion)) {
                                JsonNode asset = node.get("assets").get(0);
                                Release release = new Release(newVersion, tagName, node.get("name").asText(), asset.get("browser_download_url").asText(),
                                        asset.get("size").asInt());
                                if (releaseToUpgrade == null || newVersion.compareTo(releaseToUpgrade.version) > 0) {
                                    releaseToUpgrade = release;
                                }
                            }
                        }
                    }

                    if (releaseToUpgrade != null) {
                        setState(UPGRADE_AVAILABLE);
                        LOGGER.info("Found release {} to upgrade", releaseToUpgrade.tagName);
                    }
                }
            } else {
                LOGGER.error("Check for upgrade {} returned status {}", Configuration.GITHUB_RELEASES, con.getResponseCode());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to check upgrade", e);
        }
    }

    private static boolean isNewerVersion(Version newVersion) {
        String version = Upgrade.class.getPackage().getImplementationVersion();
        if (newVersion.compareTo(new Version(version)) > 0) {
            return true;
        }

        return false;
    }

    private static void validate(String zipFilePath) throws ValidateUpgradeException {
        String signaturePath = "app/val.sig";

        PublicKey publicKey = getPublicKey();
        Signature rsa;

        try {
            rsa = Signature.getInstance("SHA256withRSA");
            rsa.initVerify(publicKey);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new ValidateUpgradeException("Check signature of " + zipFilePath + " has failed", e);
        }

        byte[] signature = null;
        try {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry entry = zipIn.getNextEntry();

            while (entry != null) {
                if (entry.getName().endsWith(".jar")) {
                    byte[] bytesIn = new byte[4096];
                    int read;
                    while ((read = zipIn.read(bytesIn)) != -1) {
                        rsa.update(bytesIn, 0, read);
                    }
                    zipIn.closeEntry();
                } else if (signaturePath.equals(entry.getName())) {
                    signature = getBytes(zipIn);
                    zipIn.closeEntry();
                }
                entry = zipIn.getNextEntry();
            }
            zipIn.close();
        } catch (IOException | SignatureException e) {
            throw new ValidateUpgradeException("Failed to read files in " + zipFilePath, e);
        }

        if (signature == null || signature.length == 0) {
            throw new ValidateUpgradeException("Failed to read file " + signaturePath + " in " + zipFilePath);
        }

        try {
            if (!rsa.verify(signature)) {
                throw new ValidateUpgradeException("The package " + zipFilePath + " is not valid!");
            }
        } catch (SignatureException e) {
            throw new ValidateUpgradeException("Check signature of " + zipFilePath + " has failed", e);
        }
    }

    private static PublicKey getPublicKey() throws ValidateUpgradeException {
        try {
            InputStream is = Upgrade.class.getClassLoader().getResourceAsStream("public.key");
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(getBytes(is));
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            throw new ValidateUpgradeException("Failed to load public key", e);
        }
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = is.read(bytesIn)) != -1) {
            baos.write(bytesIn, 0, read);
        }
        baos.close();

        return baos.toByteArray();
    }

    private static void unzip(String zipFilePath, String destDirectory, String extract) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {
            if (entry.getName().startsWith(extract)) {
                String filePath = destDirectory + entry.getName().replace(extract, "");
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    private static URLConnection getConnection(URL url) throws IOException {
        if (proxy != null) {
            return url.openConnection(proxy);
        } else {
            return url.openConnection();
        }
    }

    private static class Release {

        private Version version;

        private String tagName;

        private String name;

        private String url;

        private int size;

        private Release(Version version, String tagName, String name, String url, int size) {
            this.version = version;
            this.tagName = tagName;
            this.name = name;
            this.url = url;
            this.size = size;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Release{");
            sb.append("tagName='").append(tagName).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append(", url='").append(url).append('\'');
            sb.append(", size=").append(size);
            sb.append(", ").append(version);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class Version implements Comparable {

        private int major;

        private int minor;

        private int patch;

        private Version(String version) {
            if (version != null) {
                Matcher matcher = VERSION_PATTERN.matcher(version);
                if (matcher.find()) {
                    major = Integer.parseInt(matcher.group("major"));
                    minor = Integer.parseInt(matcher.group("minor"));
                    patch = Integer.parseInt(matcher.group("patch"));
                }
            }
        }

        @Override
        public int compareTo(Object o) {
            if (o != null) {
                Version v = (Version) o;
                int r = this.major - v.major;
                if (r != 0) {
                    return r;
                } else {
                    r = this.minor - v.minor;
                    if (r != 0) {
                        return r;
                    } else {
                        return this.patch - v.patch;
                    }
                }
            }

            return 1;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Version{");
            sb.append("major=").append(major);
            sb.append(", minor=").append(minor);
            sb.append(", patch=").append(patch);
            sb.append('}');
            return sb.toString();
        }
    }
}
