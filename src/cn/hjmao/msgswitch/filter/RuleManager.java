package cn.hjmao.msgswitch.filter;

import java.util.ArrayList;

public class RuleManager {

	private ArrayList<String> rules = new ArrayList<String>();
	
	public RuleManager() {
		String rule = "^10658139.*";
		addRule(rule);
	}
	
	public void addRule(String rule) {
		rules.add(rule);
	}
	
	public boolean match(String sender) {
		boolean matched = false;
		
		for (String rule: rules) {
			if (sender.matches(rule)) {
				matched = true;
			}
		}
		return matched;
	}
}
