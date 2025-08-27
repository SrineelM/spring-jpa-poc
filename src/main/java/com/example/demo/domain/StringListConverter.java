package com.example.demo.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A JPA AttributeConverter to store a List of Strings as a single comma-separated String in the
 * database. This is useful for simple collections of strings where a separate table would be
 * overkill. The {@link Converter} annotation with `autoApply = true` would automatically apply this
 * converter to all attributes of type `List<String>`, but it's often better to apply it explicitly
 * via `@Convert(converter = StringListConverter.class)` on the entity field for more control.
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

  private static final String DELIMITER = ",";

  /**
   * Converts a List of Strings into a single String for database storage.
   *
   * @param attribute The list of strings from the entity.
   * @return A single, comma-separated string. Returns null if the input list is null.
   */
  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return null;
    }
    return String.join(DELIMITER, attribute);
  }

  /**
   * Converts a String from the database back into a List of Strings for the entity.
   *
   * @param dbData The comma-separated string from the database.
   * @return A List of strings. Returns an empty list if the database data is null or blank.
   */
  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return Collections.emptyList();
    }
    return Arrays.stream(dbData.split(DELIMITER)).map(String::trim).collect(Collectors.toList());
  }
}
