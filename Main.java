import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    public static void main(String[] args) {
        try {
            String[] options = {"Encode (Encrypt & Hide)", "Decode (Extract & Decrypt)"};
            int mode = JOptionPane.showOptionDialog(
                    null,
                    "Select an option:",
                    "Steganography with AES",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (mode == 0) {
                // ENCODE MODE
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Input Image (PNG)");
                int result = fileChooser.showOpenDialog(null);
                if (result != JFileChooser.APPROVE_OPTION) return;
                File inputFile = fileChooser.getSelectedFile();
                String inputImage = inputFile.getAbsolutePath();

                fileChooser.setDialogTitle("Save Encoded Image As");
                result = fileChooser.showSaveDialog(null);
                if (result != JFileChooser.APPROVE_OPTION) return;
                File outputFile = fileChooser.getSelectedFile();
                String outputImage = outputFile.getAbsolutePath();
                if (!outputImage.endsWith(".png")) outputImage += ".png";

                String secretMessage = JOptionPane.showInputDialog("Enter the secret message:");
                if (secretMessage == null || secretMessage.isEmpty()) return;

                String password = JOptionPane.showInputDialog("Enter the encryption password:");
                if (password == null || password.isEmpty()) return;

                String encryptedMessage = AESUtil.encrypt(secretMessage, password);
                SteganographyAlphaAES.encode(inputImage, outputImage, encryptedMessage);

                int zipOption = JOptionPane.showConfirmDialog(null, "Do you want to save the encoded image as a ZIP file?", "Save as ZIP", JOptionPane.YES_NO_OPTION);
                if (zipOption == JOptionPane.YES_OPTION) {
                    String zipFileName = outputImage.replace(".png", ".zip");
                    zipImage(outputImage, zipFileName);
                    JOptionPane.showMessageDialog(null, "Image saved and zipped as: " + zipFileName);
                } else {
                    JOptionPane.showMessageDialog(null, "Image saved as: " + outputImage);
                }

            } else if (mode == 1) {
                // DECODE MODE
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Encoded Image (PNG)");
                int result = fileChooser.showOpenDialog(null);
                if (result != JFileChooser.APPROVE_OPTION) return;

                File encodedFile = fileChooser.getSelectedFile();
                String encodedImage = encodedFile.getAbsolutePath();

                String password = JOptionPane.showInputDialog("Enter the decryption password:");
                if (password == null || password.isEmpty()) return;

                String extractedCipher = SteganographyAlphaAES.decode(encodedImage);
                String decryptedMessage = AESUtil.decrypt(extractedCipher, password);

                JOptionPane.showMessageDialog(null,
                        "Decrypted Message:\n" + decryptedMessage,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.exit(0);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "An error occurred: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void saveRecentIP(String ip) {
        try {
            File file = new File("recent_ips.txt");
            List<String> existing = new ArrayList<>();
            if (file.exists()) {
                existing = Files.readAllLines(file.toPath());
            }
            if (!existing.contains(ip)) {
                existing.add(ip);
                Files.write(file.toPath(), existing);
            }
        } catch (IOException e) {
            System.err.println("Failed to save recent IP: " + e.getMessage());
        }
    }

    public static String getReceiverIPFromUser() {
        List<String> recentIPs = new ArrayList<>();
        File ipFile = new File("recent_ips.txt");
        if (ipFile.exists()) {
            try {
                recentIPs = Files.readAllLines(ipFile.toPath());
            } catch (IOException ignored) {}
        }
        JComboBox<String> ipBox = new JComboBox<>();
        for (String ip : recentIPs) ipBox.addItem(ip);
        ipBox.setEditable(true);
        int result = JOptionPane.showConfirmDialog(null, ipBox, "Select or Enter Receiver IP", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return ipBox.getSelectedItem().toString().trim();
        } else {
            return null;
        }
    }

    public static void logTransaction(String filename, String receiverIP) {
        try (FileWriter fw = new FileWriter("transfer_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            String timestamp = LocalDateTime.now().toString();
            out.println("File: " + filename + " | Sent to: " + receiverIP + " | Time: " + timestamp);
        } catch (IOException e) {
            System.err.println("Failed to log transaction: " + e.getMessage());
        }
    }

    public static void zipImage(String inputImagePath, String zipFilePath) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            File fileToZip = new File(inputImagePath);
            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zos.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to zip image: " + e.getMessage());
        }
    }
} 
