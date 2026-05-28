package editor.buildingeditor2.wb;

public class FX32 {
    private int val;

    public FX32(int num)
    {
        this.val = num;
    }

    public int GetValue()
    {
        return val;
    }

    public void SetValue(int val)
    {
        this.val = val;
    }

    public float GetValueAsFloat()
    {
        return val / 4096f;
    }

    public static FX32 TryParse(double val)
    {
        return new FX32((int) (val * 4096f));
    }
}
