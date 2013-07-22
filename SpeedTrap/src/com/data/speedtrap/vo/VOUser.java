package com.data.speedtrap.vo;

import com.data.speedtrap.preference.Preference;

import android.content.Context;


public class VOUser {
	private Preference prefs;

	Context myContext;

	public VOUser(Context ctx) {
		myContext = ctx;
		if (prefs == null)
			prefs = new Preference(myContext);
	}

	public String getUserId() {
		return prefs.getPreference("selectedUserID");
	}

	public String getSelectedUserName() {
		return prefs.getPreference("selectedUserName");
	}

	public String getSelectedFirstName() {
		return prefs.getPreference("selectedFirstName");
	}

	public String getUseremail() {
		return prefs.getPreference("useremail");
	}

	public String getSelectedLanguage() {
		return prefs.getPreference("SelectedLanguage");
	}

	public void setUserId(String _UserId) {
		prefs.setPreference("selectedUserID", "" + _UserId);
	}

	public void setSelectedUserName(String _SelectedUserName) {
		prefs.setPreference("selectedUserName", "" + _SelectedUserName);
	}

	public void setSelectedFirstName(String _selectedFirstName) {
		prefs.setPreference("selectedFirstName", "" + _selectedFirstName);
	}

	public void setSelectedLanguage(String _SelectedLanguage) {
		prefs.setPreference("SelectedLanguage", "" + _SelectedLanguage);
	}

	public void setUseremail(String _useremail) {
		prefs.setPreference("useremail", "" + _useremail);
	}

}
