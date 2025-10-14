package api.dto;

import java.util.List;

/**
 * Data Transfer Object for the server response containing programs and functions
 */
public class ProgramsResponseDTO {
    private List<ProgramInfoDTO> programs;
    private List<FunctionInfoDTO> functions;

    public ProgramsResponseDTO() {}

    public List<ProgramInfoDTO> getPrograms() { return programs; }
    public void setPrograms(List<ProgramInfoDTO> programs) { this.programs = programs; }

    public List<FunctionInfoDTO> getFunctions() { return functions; }
    public void setFunctions(List<FunctionInfoDTO> functions) { this.functions = functions; }
}
