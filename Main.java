import javax.swing.*;
import java.io.File;

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
                // Encode Mode
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
                if (!outputImage.endsWith(".png")) {
                    outputImage += ".png";
                }

                String secretMessage = JOptionPane.showInputDialog("Enter the secret message:");
                if (secretMessage == null || secretMessage.isEmpty()) return;

                String password = JOptionPane.showInputDialog("Enter the encryption password:");
                if (password == null || password.isEmpty()) return;

                String encryptedMessage = AESUtil.encrypt(secretMessage, password);
                SteganographyAlphaAES.encode(inputImage, outputImage, encryptedMessage);

                JOptionPane.showMessageDialog(null,
                        "Message successfully hidden in " + outputImage,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } else if (mode == 1) {
                // Decode Mode
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
                // User closed the dialog
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
}
