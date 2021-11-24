package uk.nhs.nhsx.covid19.android.app.questionnaire.selection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uk.nhs.nhsx.covid19.android.app.R
import uk.nhs.nhsx.covid19.android.app.databinding.ItemQuestionBinding
import uk.nhs.nhsx.covid19.android.app.questionnaire.review.adapter.ReviewSymptomItem.Question
import uk.nhs.nhsx.covid19.android.app.questionnaire.selection.adapter.QuestionnaireViewAdapter.QuestionnaireViewHolder
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setOnSingleClickListener
import uk.nhs.nhsx.covid19.android.app.util.viewutils.setUpAccessibilityHeading

class QuestionnaireViewAdapter(
    private val listener: (Question) -> Unit
) :
    ListAdapter<Question, QuestionnaireViewHolder>(ItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionnaireViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemQuestionBinding.inflate(inflater, parent, false)
        return QuestionnaireViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: QuestionnaireViewHolder, position: Int) =
        holder.bind(getItem(position), listener)

    class QuestionnaireViewHolder(
        private val itemBinding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(question: Question, listener: (Question) -> Unit) = with(itemBinding) {
            checkboxQuestion.text = question.symptom.title.translate()
            textQuestionDescription.text = question.symptom.description.translate()
            checkboxQuestion.isChecked = question.isChecked
            val background = if (question.isChecked) {
                R.drawable.question_selected_background
            } else {
                R.drawable.question_not_selected_background
            }
            questionContainer.background = root.context.getDrawable(background)

            questionContainer.setOnSingleClickListener {
                listener(question)
            }

            checkboxQuestion.setUpAccessibilityHeading()
        }
    }

    class ItemCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question) =
            oldItem.symptom == newItem.symptom

        override fun areContentsTheSame(oldItem: Question, newItem: Question) =
            oldItem.isChecked == newItem.isChecked

        override fun getChangePayload(oldItem: Question, newItem: Question): Any? {
            return CheckBoxStateChanged
        }
    }

    companion object {
        object CheckBoxStateChanged
    }
}
