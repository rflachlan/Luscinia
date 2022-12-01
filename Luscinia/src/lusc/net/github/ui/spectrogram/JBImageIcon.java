package lusc.net.github.ui.spectrogram;


/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * HiDPI-aware image icon
 *
 * @author Konstantin Bulenkov
 */
public class JBImageIcon extends ImageIcon {
  
	public JBImageIcon(Image image) {
    super(image);
  }

  @Override
  public synchronized void paintIcon(final Component c, final Graphics g, final int x, final int y) {
    final ImageObserver observer = c;
/*
    Image image=getImage();
    final Graphics2D newG = (Graphics2D) g.create(x, y, image.getWidth(observer), image.getHeight(observer));
    newG.scale(0.5, 0.5);
    Image img = ((JBHiDPIScaledImage) image).getDelegate();
    if (img == null) {
    	img = image;
    }
    newG.drawImage(img, 0, 0, observer);
    newG.scale(1, 1);
    newG.dispose();
    */
  }
}