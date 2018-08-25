package oocourse.system;

public class FileNameCheck {
	
	public static String check(String name) {
		
		String checkedName = "";
		
		int len = name.length();
		
		char buf;
		
		for(int i = 0; i < len; ++i) {
			buf = name.charAt(i);
			if(buf != '*' && buf != '?') {
				checkedName = checkedName + buf;
			}
		}
		
		return checkedName;
	}
}
