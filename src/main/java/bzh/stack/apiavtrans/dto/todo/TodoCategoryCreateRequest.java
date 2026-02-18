package bzh.stack.apiavtrans.dto.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoCategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be in hexadecimal format (#RRGGBB)")
    private String color;
}
