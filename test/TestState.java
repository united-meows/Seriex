package test;

public enum TestState {
	SUCCESS,
	WARNING,
	FAIL,
	FATAL_ERROR;

	public boolean isSpecial() {
		return this == TestState.WARNING || this == TestState.FATAL_ERROR;
	}
}
