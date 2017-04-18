package CLpack;

import java.awt.*;

import javax.swing.JComponent;

class Canvas extends JComponent{ //+сделать отключаемым
	 
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 
	public int[] colors;
	
 	Graphics2D g2d;
 	
 	Canvas(int[] _colors){
 		this.colors = _colors; 
 	}
 	
 	public void paintComponent(Graphics g){	//=проверить позиционирование

	g2d=(Graphics2D)g;
	
	//colors = CL.getColors();
	
	for(int i = 0; i<CL.mapHeight; i++){
		for(int j = 0; j<CL.mapWidth; j++){
			g2d.setPaint(Color.decode("#"+Integer.toHexString(colors[i*CL.mapHeight+j])));
			if(CL.scale>3){
				g2d.drawRect(j*CL.scale, i*CL.scale, CL.scale-1, CL.scale-1);
				g2d.fillRect(j*CL.scale, i*CL.scale, CL.scale-1, CL.scale-1);
			}else{
				g2d.drawRect(j*CL.scale, i*CL.scale, CL.scale-1, CL.scale-1);
			}
		}
	}
  }	
}
