/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package smartai.examples.vision;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ViewerFrame {

    private JFrame frame;
    private ImagePanel imagePanel;

    public ViewerFrame(int width, int height) {
        frame = new JFrame("Demo");
        imagePanel = new ImagePanel();
        frame.setLayout(new BorderLayout());
        frame.add(BorderLayout.CENTER, imagePanel);

        JOptionPane.setRootFrame(frame);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (width > screenSize.width) {
            width = screenSize.width;
        }
        Dimension frameSize = new Dimension(width, height);
        frame.setSize(frameSize);
        frame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void showImage(BufferedImage image) {
        imagePanel.setImage(image);
        SwingUtilities.invokeLater(
                () -> {
                    frame.repaint();
                    frame.pack();
                });
    }

    private static final class ImagePanel extends JPanel {

        private BufferedImage image;

        void setImage(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                return;
            }

            g.drawImage(image, 0, 0, null);
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
    }
}
