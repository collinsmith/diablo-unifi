package com.gmail.collinsmith70.validator;

import android.support.annotation.NonNull;

@SuppressWarnings("unused")
public class NonNullNonEmptyStringValidator extends NonNullValidator {

  @Override
  public void validate(@NonNull Object obj) {
    super.validate(obj);
    if (obj.getClass() != String.class) {
      throw new ValidationException("obj is not a String");
    }

    String str = (String) obj;
    if (str.isEmpty()) {
      throw new ValidationException("passed String is empty");
    }
  }

}
