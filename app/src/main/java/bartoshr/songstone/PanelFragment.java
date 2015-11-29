package bartoshr.songstone;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


public class PanelFragment extends Fragment {

    public OnAnimationChanged animationChangedListener;
    public OnPanelClickListener onPanelClickListener;

    public PanelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_panel, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        String text = bundle.getString(MainActivity.BUNDLE_TEXT, "");

        TextView textView = (TextView) getView().findViewById(R.id.panelView);

        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onPanelClickListener.onPanelClick();
            }
        });

        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return onPanelClickListener.onPanelLongCLick();
            }
        });

        textView.setText(text);
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Animator animator = super.onCreateAnimator(transit, enter, nextAnim);

        if (animator == null && nextAnim != 0) {
            animator = AnimatorInflater.loadAnimator(getActivity(), nextAnim);
        }


        if(animator != null) {

            animator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animator) {
                    animationChangedListener.onAnimationStarted();
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    animationChangedListener.onAnimationEnded();
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
        }

        return animator;
    }

    public void setAnimationChangedListener(OnAnimationChanged animationChangedListener) {
        this.animationChangedListener = animationChangedListener;
    }

    public void setOnPanelClickListener(OnPanelClickListener onPanelClickListener){
        this.onPanelClickListener = onPanelClickListener;
    }


    public interface OnAnimationChanged{
        public void onAnimationEnded();
        public void onAnimationStarted();
    }

    public interface OnPanelClickListener{
        public void onPanelClick();
        public boolean onPanelLongCLick();
    }
}
