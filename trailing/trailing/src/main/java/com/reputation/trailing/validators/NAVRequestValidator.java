package com.reputation.trailing.validators;

import java.util.Set;
import java.util.StringJoiner;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.reputation.trailing.TrailingException;
import com.reputation.trailing.models.Trailing;

@Component("trailingValidator")
public class NAVRequestValidator implements IRequestValidator<Trailing> {
	private Logger logger = LoggerFactory.getLogger(NAVRequestValidator.class);


	@Override
	public Trailing validateReqParam(final Trailing trail) throws TrailingException {

		try {
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<Trailing>> violations = validator.validate(trail);
			if (!violations.isEmpty()) {
				StringJoiner joiner = new StringJoiner(",");
				for (ConstraintViolation<Trailing> violation : violations) {
					joiner.add(violation.getMessage());
				}
				throw new TrailingException(HttpStatus.BAD_REQUEST.toString(), joiner.toString());
			}
			return trail;
		} catch (Exception e) {
			throw new TrailingException(HttpStatus.BAD_REQUEST.toString(),
					"Failed to validate request - " + e.getMessage());
		}
	}

}
