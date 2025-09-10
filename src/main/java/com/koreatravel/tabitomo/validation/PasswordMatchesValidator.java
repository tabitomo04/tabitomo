package com.koreatravel.tabitomo.validation;

import com.koreatravel.tabitomo.domain.dto.member.MemberFormDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator 
  implements ConstraintValidator<PasswordMatches, Object> { 
    
    @Override
    public void initialize(PasswordMatches constraintAnnotation) {       
    }
    
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){   
        MemberFormDTO user = (MemberFormDTO) obj;
        return user.getPassword().equals(user.getPasswordConfirm());    
    }     
}
