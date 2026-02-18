package bzh.stack.apiavtrans.dto.absence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceTypeCreateRequest {

    @NotBlank(message = "Le nom du type est obligatoire")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "La couleur doit être au format hexadécimal (#RRGGBB)")
    private String color;
}
