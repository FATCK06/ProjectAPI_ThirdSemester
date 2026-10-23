package br.com.newe.ms_operacoes.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

/** Mes no formato AAAA-MM, com mes de 01 a 12. Mesma regra em todos os endpoints. */
@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])")
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MesReferencia {

    String message() default "use o formato AAAA-MM";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
