package fast.common.jira.entities;

import java.util.ArrayList;

public class VersionBoard {
	private ArrayList<Version> unreleasedVersions;
	
	private ArrayList<Version> releasedVersions;

	public ArrayList<Version> getUnreleasedVersions() {
		return unreleasedVersions;
	}

	public void setUnreleasedVersions(ArrayList<Version> unreleasedVersions) {
		this.unreleasedVersions = unreleasedVersions;
	}

	public ArrayList<Version> getReleasedVersions() {
		return releasedVersions;
	}

	public void setReleasedVersions(ArrayList<Version> releasedVersions) {
		this.releasedVersions = releasedVersions;
	}
	
}
