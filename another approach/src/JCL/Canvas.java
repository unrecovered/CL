package JCL;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
//import java.awt.*;
import javax.swing.JComponent;

class Canvas extends JComponent{ //+сделать отключаемым
	 
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static BufferedImage field;
 
	public int[] colors;
	
 	Graphics2D g2d;
 	
 	Canvas(int[] _colors){
 		this.colors = _colors; 
 	}
 	
 	public void paintComponent(Graphics g){	//=проверить позиционирование	
		g2d=(Graphics2D)g;
		
		field = new BufferedImage(mvnlike.W*mvnlike.scale, mvnlike.H*mvnlike.scale,
	            BufferedImage.TYPE_INT_RGB);
	
		for(int i = 0; i<mvnlike.H; i++){
			for(int j = 0; j<mvnlike.W; j++){
				drawbact(j, i);
			}
		}
	
		g2d.drawImage(field, null, 0, 0);
 	}	
 	
 	void drawbact(int x, int y){
		for(int i = 0; i<mvnlike.scale; i++){
			for(int j=0; j<mvnlike.scale; j++){
				field.setRGB(x*mvnlike.scale+i, 
						y*mvnlike.scale+j, 
							colors[x+y*mvnlike.W]);
			}
		}
 	}
}
