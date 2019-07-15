package com.speuce.upgrades;

import com.speuce.sql.DataType;

public enum UpgradeType {
	TIMED(DataType.BIGINT),
	LEVELED(DataType.INT);
	private DataType type;
	private UpgradeType(DataType type){
		this.type = type;
	}
	public DataType getType() {
		return type;
	}
}
