package dan2097.org.bitbucket.rxdemo;

import java.io.File;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.element.CMLReaction;

import com.ggasoftware.indigo.Indigo;
import com.ggasoftware.indigo.IndigoObject;
import com.ggasoftware.indigo.IndigoRenderer;

import uk.ac.cam.ch.wwmm.opsin.XOMFormatter;

public class HtmlGenerator {

	private static final String IDENTIFIER = "identifier";
	
	public static String generateHtml(CMLReaction reaction) {
		Element html = new Element("html");
		Element head = new Element("head");
		html.appendChild(head);
		Element body = new Element("body");
		html.appendChild(body);
		Element inputTextH2 = new Element("h2");
		inputTextH2.appendChild("Input Text");
		body.appendChild(inputTextH2);
		Element source = new Element("div");
		Element heading =reaction.getFirstChildElement("source").getFirstChildElement("heading");
		source.appendChild(heading.getAttributeValue("title"));
		source.appendChild(new Element("br"));
		source.appendChild(new Element("br"));
		source.appendChild(heading.getFirstChildElement("p").getValue());
		body.appendChild(source);
		Elements products = reaction.getFirstChildElement("productList", CMLConstants.CML_NS).getChildElements("product", CMLConstants.CML_NS);
		Element productH2 = new Element("h2");
		productH2.appendChild("Product");
		body.appendChild(productH2);
		addMoleculeSummaryTableToBody(body, products);
		
		Elements reactants = reaction.getFirstChildElement("reactantList", CMLConstants.CML_NS).getChildElements("reactant", CMLConstants.CML_NS);
		Element reactantH2 = new Element("h2");
		reactantH2.appendChild("Reactants");
		body.appendChild(reactantH2);
		addMoleculeSummaryTableToBody(body, reactants);
		
		Elements spectators = reaction.getFirstChildElement("spectatorList", CMLConstants.CML_NS).getChildElements("spectator", CMLConstants.CML_NS);
		Element spectatorH2 = new Element("h2");
		spectatorH2.appendChild("Spectators");
		body.appendChild(spectatorH2);
		addMoleculeSummaryTableToBody(body, spectators);
		Element rawXMLH2 = new Element("h2");
		rawXMLH2.appendChild("Raw XML");
		body.appendChild(rawXMLH2);
		Element rawXML = new Element("pre");
		XOMFormatter xomFormatter = new XOMFormatter();
		rawXML.appendChild(xomFormatter.elemToString(reaction));
		body.appendChild(rawXML);
		return html.toXML();
	}

	private static void addMoleculeSummaryTableToBody(Element body, Elements parentEls) {
		Element productsDiv = new Element("div");
		for (int i = 0; i < parentEls.size(); i++) {
			Element molecule = parentEls.get(i).getFirstChildElement("molecule", CMLConstants.CML_NS);
			Element table = new Element("table");
			addNameRowToTable(molecule, table);
			addStructureRowToTable(molecule, table);
			addAmountRowsToTable(parentEls.get(i).getChildElements("amount", CMLConstants.CML_NS), table);
			allRoleIfKnown(parentEls.get(i), table);
			productsDiv.appendChild(table);
		}
		body.appendChild(productsDiv);
	}

	private static void addNameRowToTable(Element molecule, Element table) {
		Element row = new Element("tr");
		Element cellLeft = new Element("td");
		cellLeft.appendChild("Name");
		Element cellRight = new Element("td");
		cellRight.addAttribute(new Attribute("style", "font-weight:bold"));
		cellRight.appendChild(molecule.getAttributeValue("title"));
		row.appendChild(cellLeft);
		row.appendChild(cellRight);
		table.appendChild(row);
	}
	
	private static void addStructureRowToTable(Element molecule, Element table) {
		Element row = new Element("tr");
		Element cellLeft = new Element("td");
		cellLeft.appendChild("Structure");
		Element cellRight = new Element("td");
		Element imageEl = new Element("img");
		File file = generateImageAndReturnFileIndigo(molecule);
		if (file==null){
			imageEl.addAttribute(new Attribute("alt", "failed to generate depiction"));
		}
		else{
			imageEl.addAttribute(new Attribute("src", "file:///" + file.getAbsolutePath()));
		}
		cellRight.appendChild(imageEl);
		row.appendChild(cellLeft);
		row.appendChild(cellRight);
		table.appendChild(row);
	}
	private static File generateImageAndReturnFileIndigo(Element molecule) {
		Element identifier = molecule.getFirstChildElement(IDENTIFIER);
		if (identifier==null){
			return null;
		}
		else{
			try{
				Indigo indigo = new Indigo();
				IndigoRenderer renderer = new IndigoRenderer(indigo);
				indigo.setOption("render-output-format", "png");
				indigo.setOption("render-coloring", true);
				indigo.setOption("render-stereo-old-style", true);
				IndigoObject mol = indigo.loadMolecule(identifier.getValue());
				File f = File.createTempFile("chemical_", ".png");
				renderer.renderToFile(mol, f.getAbsolutePath());
				return f;
			}
			catch (Exception e) {
			}
			
		}
		return null;
	}

	private static void addAmountRowsToTable(Elements amountEls, Element table) {
		for (int i = 0; i < amountEls.size(); i++) {
			Element amountEl =amountEls.get(i);
			Element row = new Element("tr");
			Element cellLeft = new Element("td");
			cellLeft.appendChild("amount/units");
			Element cellRight = new Element("td");
			cellRight.appendChild(amountEl.getValue()+"/"+amountEl.getAttributeValue("units").split(":")[1]);
			row.appendChild(cellLeft);
			row.appendChild(cellRight);
			table.appendChild(row);
		}
	}
	
	private static void allRoleIfKnown(Element parentEl, Element table) {
		String role = parentEl.getAttributeValue("role");
		if (role!=null){
			Element row = new Element("tr");
			Element cellLeft = new Element("td");
			cellLeft.appendChild("Role");
			Element cellRight = new Element("td");
			cellRight.appendChild(role);
			row.appendChild(cellLeft);
			row.appendChild(cellRight);
			table.appendChild(row);
		}
	}
}
