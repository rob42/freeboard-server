package nz.co.fortytwo.freeboard.zk;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Panel;

public class InstrumentViewModel extends SelectorComposer<Component>{

	
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Wire ("#logg")
	Panel logg;
	@Wire("#wind")
	Panel wind;
	@Wire("#chartplotter")
	Panel chartplotter;

	@Init
	public void init() {
		//all hidden and in left corner
		logg.setFloatable(true);
		wind.setFloatable(true);
		chartplotter.setFloatable(true);
	}

}
