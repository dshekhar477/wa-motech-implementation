package org.motechproject.wa.imi.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.wa.imi.domain.ChunkAuditRecord;

public interface ChunkAuditRecordDataService extends MotechDataService<ChunkAuditRecord> {
    @Lookup
    ChunkAuditRecord findByFileAndChunk(@LookupField(name = "file") String file,
                                        @LookupField(name = "chunk") String chunk);
}
