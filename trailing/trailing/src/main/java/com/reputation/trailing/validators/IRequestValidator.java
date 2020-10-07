package com.reputation.trailing.validators;

import com.reputation.trailing.TrailingException;

public interface IRequestValidator<T> {
	
	T validateReqParam(T reqData) throws TrailingException;
}
