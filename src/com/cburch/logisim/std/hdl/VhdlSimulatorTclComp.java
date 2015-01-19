/*******************************************************************************
 * This file is part of logisim-evolution.
 *
 *   logisim-evolution is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   logisim-evolution is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with logisim-evolution.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Original code by Carl Burch (http://www.cburch.com), 2011.
 *   Subsequent modifications by :
 *     + Haute École Spécialisée Bernoise
 *       http://www.bfh.ch
 *     + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *       http://hepia.hesge.ch/
 *     + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *       http://www.heig-vd.ch/
 *   The project is currently maintained by :
 *     + REDS Institute - HEIG-VD
 *       Yverdon-les-Bains, Switzerland
 *       http://reds.heig-vd.ch
 *******************************************************************************/
package com.cburch.logisim.std.hdl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.util.FileUtil;
import com.cburch.logisim.util.LocaleManager;

/**
 * The VHDL source file have to be compiled before they can be simulated. This
 * is done by the following generated script. The script is called by the
 * run.tcl script that is in the resource folders.
 *
 * @author christian.mueller@heig-vd.ch
 */
class VhdlSimulatorTclComp {

	final static Logger logger = LoggerFactory
			.getLogger(VhdlSimulatorTclComp.class);

	private boolean valid = false;
	private VhdlSimulator vhdlSimulator;

	VhdlSimulatorTclComp(VhdlSimulator vs) {
		vhdlSimulator = vs;
	}

	public void fireInvalidated() {
		valid = false;
	}

	public void generate() {

		/* Do not generate if file is already valid */
		if (valid)
			return;

		StringBuilder comp_files = new StringBuilder();
		comp_files.append("Autogenerated by logisim");
		comp_files.append(System.getProperty("line.separator"));

		/* For each vhdl entity */
		for (Component comp : VhdlSimulator.getVhdlComponents(vhdlSimulator
				.getProject().getCircuitState())) {
			if (comp.getFactory().getClass().equals(VhdlEntity.class)) {

				InstanceState state = vhdlSimulator.getProject()
						.getCircuitState().getInstanceState(comp);
				String componentName = comp.getFactory().getHDLTopName(
						state.getInstance().getAttributeSet());

				comp_files.append("vcom -reportprogress 300 -work work ../src/"
						+ componentName + ".vhdl");
				comp_files.append(System.getProperty("line.separator"));
			}
		}

		/*
		 * Replace template blocks by generated data
		 */
		String template;
		try {
			template = new String(FileUtil.getBytes(this.getClass()
					.getResourceAsStream(
							(VhdlSimulator.SIM_RESOURCES_PATH + "comp.templ"))));

			template = template.replaceAll("%date%",
					LocaleManager.parserSDF.format(new Date()));
			template = template.replaceAll("%comp_files%",
					comp_files.toString());

		} catch (IOException e) {
			logger.error("Could not read template : {}", e.getMessage());
			return;
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(VhdlSimulator.SIM_PATH + "comp.tcl",
					"UTF-8");
			writer.print(template);
			writer.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not create run.tcl file : {}", e.getMessage());
			e.printStackTrace();
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error("Could not create run.tcl file : {}", e.getMessage());
			e.printStackTrace();
			return;
		}

		valid = true;
	}
}