package rs.edu.raf.si.bank2.otc.requests;

import lombok.Data;
import rs.edu.raf.si.bank2.otc.models.mariadb.PermissionName;

@Data
public class CheckPermissionRequest {
    PermissionName permissionName;
    String userEmail;
}
