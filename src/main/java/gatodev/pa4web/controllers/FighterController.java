package gatodev.pa4web.controllers;

import gatodev.pa4web.DTO.FighterDTO;
import gatodev.pa4web.models.Academy;
import gatodev.pa4web.models.Fighter;
import gatodev.pa4web.models.League;
import gatodev.pa4web.services.FighterService;
import gatodev.pa4web.services.FighterServiceImpl;
import gatodev.pa4web.services.generic.AcademyServiceImpl;
import gatodev.pa4web.services.generic.GenericService;
import gatodev.pa4web.services.generic.LeagueServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, 
                 maxFileSize = 5 * 1024 * 1024,    
                 maxRequestSize = 6 * 1024 * 1024)
@WebServlet(name = "FighterController", urlPatterns = "/fighter")
public class FighterController extends HttpServlet {
    private final GenericService<Academy> academyService = AcademyServiceImpl.instance;
    private final GenericService<League> leagueService = LeagueServiceImpl.instance;
    private final FighterService fighterService = FighterServiceImpl.instance;

    private String uploadDir;

    @Override
    public void init() {
        uploadDir = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<FighterDTO> fighters = fighterService.getAllFighters()
                .stream()
                .map(f -> fighterService.convertToFighterDTO(f,
                        academyService.get(f.getIdAcademy()),
                        leagueService.get(f.getIdLeague())
                ))
                .toList();

        req.setAttribute("fighters", fighters);
        req.setAttribute("academies", academyService.getAll());
        req.setAttribute("leagues", leagueService.getAll());
        req.getRequestDispatcher("fighter.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String method = req.getParameter("_method");

        // Parse common fields
        String idStr = req.getParameter("id");
        String dni = req.getParameter("dni");
        String fullName = req.getParameter("fullName");
        String ageStr = req.getParameter("age");
        String weightStr = req.getParameter("weight");
        String gender = req.getParameter("gender");
        String rank = req.getParameter("rank");
        String modality = req.getParameter("modality");
        String idAcademyStr = req.getParameter("idAcademy");
        String idLeagueStr = req.getParameter("idLeague");

        // Validaciones
        if (dni == null || fullName == null || ageStr == null || weightStr == null
                || gender == null || rank == null || modality == null
                || idAcademyStr == null || idLeagueStr == null
                || dni.isBlank() || fullName.isBlank()
                || ageStr.isBlank() || weightStr.isBlank()
                || gender.isBlank() || rank.isBlank()
                || modality.isBlank()
                || idAcademyStr.isBlank() || idLeagueStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Todos los campos (excepto foto en edición) son obligatorios.");
            return;
        }

        int age, idAcademy, idLeague;
        double weight;
        try {
            age = Integer.parseInt(ageStr);
            idAcademy = Integer.parseInt(idAcademyStr);
            idLeague = Integer.parseInt(idLeagueStr);
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Edad, peso, academia o liga inválidos.");
            return;
        }

        Part photoPart = req.getPart("photo");
        String photoFilename = null;
        if (photoPart != null && photoPart.getSize() > 0) {
            String submittedName = Path.of(photoPart.getSubmittedFileName()).getFileName().toString();
            photoFilename = System.currentTimeMillis() + "-" + submittedName;
            Path filePath = Paths.get(uploadDir, photoFilename);
            try (InputStream is = photoPart.getInputStream()) {
                Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Fighter fighter = new Fighter();
        if (idStr != null && !idStr.isBlank()) {
            fighter.setId(Integer.parseInt(idStr));
        }
        fighter.setDni(dni);
        fighter.setFullName(fullName);
        fighter.setAge(age);
        fighter.setWeight(weight);
        fighter.setGender(gender);
        fighter.setRank(rank);
        fighter.setModality(modality);
        fighter.setIdAcademy(idAcademy);
        fighter.setIdLeague(idLeague);
        if (photoFilename != null) {
            fighter.setPhoto(photoFilename);
        }

        try {
            if ("update".equalsIgnoreCase(method)) {
                fighterService.updateFighter(fighter);
            } else {
                fighterService.addFighter(fighter);
            }
        } catch (RuntimeException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/fighter");
    }
}
