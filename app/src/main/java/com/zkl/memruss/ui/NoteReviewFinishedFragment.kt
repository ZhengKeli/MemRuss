package com.zkl.memruss.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zkl.memruss.R
import kotlinx.android.synthetic.main.fragment_note_review_finished.*


class NoteReviewFinishedFragment : Fragment() {
	
	companion object {
		fun newInstance(): NoteReviewFinishedFragment {
			return NoteReviewFinishedFragment::class.java.newInstance()
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_note_review_finished, container, false)
	}
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		b_back.setOnClickListener {
			fragmentManager.popBackStack()
		}
	}
}
