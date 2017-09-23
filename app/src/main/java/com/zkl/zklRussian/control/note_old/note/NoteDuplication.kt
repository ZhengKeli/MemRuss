package com.zkl.zklRussian.control.note_old.note

import java.util.*


class NoteDuplication {
	enum class Similarity { same, conflict, different }
	enum class BodyRemain { remainNew, remainOld, remainBoth, remainNone, }
	enum class ProgressRemain { remainNew, remainOld, remainMax, remainMin, remainNone }
	open class DuplicationDeal {
		var remainOld = true
		var toAdds = ArrayList<LocalNote>(2)
		
		constructor(remainOld: Boolean, toAdds: ArrayList<LocalNote>) {
			this.remainOld = remainOld
			this.toAdds.addAll(toAdds)
		}
		
		constructor(oldNote: LocalNote, newNote: LocalNote, body: BodyRemain, memoryProgress: ProgressRemain) {
			if (body == BodyRemain.remainNone) {
				remainOld = false
			} else if (body == BodyRemain.remainBoth) {
				remainOld = true
				toAdds.add(newNote)
			} else {
				//先确定出 memoryProgress
				val newProgress: Memory.NoteProgress?=
				when (memoryProgress) {
					ProgressRemain.remainNew -> newNote.noteProgress
					ProgressRemain.remainOld -> oldNote.noteProgress
					ProgressRemain.remainMax -> Memory.NoteProgress.getMaximum(newNote.noteProgress, oldNote.noteProgress)
					ProgressRemain.remainMin -> Memory.NoteProgress.getMinimum(newNote.noteProgress, oldNote.noteProgress)
					ProgressRemain.remainNone ->null
				}

				//组装 toAdd
				val toAdd: LocalNote? = when (body) {
					NoteDuplication.BodyRemain.remainNew -> newNote.getClone()
					NoteDuplication.BodyRemain.remainOld -> oldNote.getClone(noteProgress = newProgress)
					else -> null
				}

				//执行添加和删除
				if (toAdd != null) {
					remainOld = false
					toAdds.add(toAdd)
				}
			}
		}
	}
}
