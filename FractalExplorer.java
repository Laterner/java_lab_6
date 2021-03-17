import java.awt.*;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import javax.swing.filechooser.*;
import java.awt.image.*;

public class FractalExplorer
{
    // Количество строк, оставшихся не нарисованными.
    private int rowsRemaining;

    // Размер экрана в пикселях
    private int displaySize;

    private JImageDisplay display;

    private FractalGenerator fractal;

    private Rectangle2D.Double range;

    private JButton saveButton;
    private JButton resetButton;
    private JComboBox myComboBox;

    public FractalExplorer(int size)
    {
        // Задаём размер дисплея
        displaySize = size;

        // Инициализация FractalGenerator
        fractal = new Mandelbrot();
        // Задаём диапазон
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        // Создаём новый дисплей
        display = new JImageDisplay(displaySize, displaySize);
    }

    // Создание окна
    public void createAndShowGUI()
    {
        display.setLayout(new BorderLayout());
        JFrame myFrame = new JFrame("Fractal Explorer");

        myFrame.add(display, BorderLayout.CENTER);

        resetButton = new JButton("Reset");

        /** Инициализация событий для кнопок **/
        ButtonHandler resetHandler = new ButtonHandler();
        resetButton.addActionListener(resetHandler);

        /** Инициализация событий для нажатия мыши. **/
        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        /** Устанавливаем операцию закрытия фрейма по умолчанию на обычный выход. **/
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /** Создание поля со списком. **/
        myComboBox = new JComboBox();

        /** Добавление фракталов в список. **/
        FractalGenerator mandelbrotFractal = new Mandelbrot();
        myComboBox.addItem(mandelbrotFractal);
        FractalGenerator tricornFractal = new Tricorn();
        myComboBox.addItem(tricornFractal);
        FractalGenerator burningShipFractal = new BurningShip();
        myComboBox.addItem(burningShipFractal);

        /** Инициализация событий при выборе фрактала. **/
        ButtonHandler fractalChooser = new ButtonHandler();
        myComboBox.addActionListener(fractalChooser);

        JPanel myPanel = new JPanel();
        JLabel myLabel = new JLabel("Fractal:");
        myPanel.add(myLabel);
        myPanel.add(myComboBox);
        myFrame.add(myPanel, BorderLayout.NORTH);

        /**
         * Создание кнопки сохранения и добавление её в JPanel
         * в позиции BorderLayout.SOUTH вместе с кнопкой сброса.
         */
        saveButton = new JButton("Save");
        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(saveButton);
        myBottomPanel.add(resetButton);
        myFrame.add(myBottomPanel, BorderLayout.SOUTH);

        /** Экземпляр ButtonHandler для кнопки сохранения. **/
        ButtonHandler saveHandler = new ButtonHandler();
        saveButton.addActionListener(saveHandler);


        myFrame.pack();
        myFrame.setVisible(true);
        myFrame.setResizable(false);
    }

    private void drawFractal()
    {
        // Вызываем enableUI(false), чтобы отключить все
        // элементы управления пользовательского интерфейса во время рисования.
        enableUI(false);

        // Установите rowsRemaining на общее количество строк.
        rowsRemaining = displaySize;

        // Проходим по каждой строке на дисплее
        // и вызываем FractalWorker, чтобы нарисовать ее.
        for (int x=0; x<displaySize; x++){
            FractalWorker drawRow = new FractalWorker(x);
            drawRow.execute();
        }
    }
    private void enableUI(boolean val) {
        myComboBox.setEnabled(val);
        resetButton.setEnabled(val);
        saveButton.setEnabled(val);
    }

    // Обработчик нажатия кновки мыши
    private class MouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            // Перевод позиции мыши в Х координату
            int x = e.getX();

            double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, x);

            int y = e.getY();
            double yCoord = fractal.getCoord(range.y, range.y + range.height, displaySize, y);

            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);

            drawFractal();
        }
    }

    private class ButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            /** Получение команды. **/
            String command = e.getActionCommand();

            /** Если команда на изменение фрактала. **/
            if (e.getSource() instanceof JComboBox) {
                JComboBox mySource = (JComboBox) e.getSource();
                fractal = (FractalGenerator) mySource.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();

            }
            /** Если команда на сброс. **/
            else if (command.equals("Reset")) {
                fractal.getInitialRange(range);
                drawFractal();
            }
            /** Если команда на сохранение. **/
            else if (command.equals("Save")) {

                JFileChooser myFileChooser = new JFileChooser();

                /** Сохранить только PNG файлы. **/
                FileFilter extensionFilter = new FileNameExtensionFilter("Image files (*.png)", "*.png");
                myFileChooser.setFileFilter(extensionFilter);

                /** Разрешить пользователю выбрать файл для сохранения изображения. **/
                myFileChooser.setAcceptAllFileFilterUsed(false);

                /** Всплывает окно «Сохранить файл», в котором пользователь
                 * может выбрать каталог и файл для сохранения. **/
                int userSelection = myFileChooser.showSaveDialog(display);

                /** Если результатом операции выбора файла является APPROVE_OPTION,
                 * продолжите операцию сохранения файла. **/
                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    /** Get the file and file name. **/
                    java.io.File file = myFileChooser.getSelectedFile();

                    String file_name = file.getName();

                    /** Попытка сохранить изображение. **/
                    try {
                        BufferedImage displayImage = display.getImage();
                        javax.imageio.ImageIO.write(displayImage, "png", file);
                    }
                    /** Получение всех ошибок и вывод их в сообщении. **/
                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(display,
                                exception.getMessage(), "Невозможно сохранить картинку",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                /** Если результат операции сохранения файла не APPROVE_OPTION - выход из функции. **/
                else return;
            }
        }
    }


    /**
     * Вычисляет значения цвета для одной строки фрактала.
     */
    private class FractalWorker extends SwingWorker<Object, Object>
    {
        /**
         * Поле для целочисленной координаты Y строки,
         * которая будет вычислена.
         */
        int yCoordinate;

        /**
         * Массив целых чисел для хранения вычисленных
         * значений RGB для каждого пикселя в строке.
         */
        int[] computedRGBValues;

        /**
         * Конструктор принимает координату Y в качестве аргумента и сохраняет ее.
         */
        private FractalWorker(int row) {
            yCoordinate = row;
        }

        /**
         * Метод вызывается в фоновом потоке.
         * Он вычисляет значение RGB для всех пикселей в 1 строке
         * и сохраняет его в соответствующем элементе целочисленного массива.
         * Возвращает null.
         */
        protected Object doInBackground() {

            computedRGBValues = new int[displaySize];

            // Перебираем все пиксели в строке.
            for (int i = 0; i < computedRGBValues.length; i++) {

                // Нахождение соответствующих координат xCoord и yCoord
                // в области отображения фрактала.
                double xCoord = fractal.getCoord(range.x,
                        range.x + range.width, displaySize, i);
                double yCoord = fractal.getCoord(range.y,
                        range.y + range.height, displaySize, yCoordinate);

                // Вычислите количество итераций для координат
                // в области отображения фрактала.
                int iteration = fractal.numIterations(xCoord, yCoord);

                // Если количество итераций равно -1, устанавливаем текущий int
                // в массиве int вычисленных значений RGB на черный.
                if (iteration == -1){
                    computedRGBValues[i] = 0;
                }

                else {
                    // Иначе выберите значение оттенка в зависимости от количества итераций.
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);

                    // Добавление цвета в массив вычисленных цветов
                    computedRGBValues[i] = rgbColor;
                }
            }
            return null;

        }
        /**
         * Вызывается, когда фоновая задача завершена.
         * Рисует пиксели для текущей строки и обновляет отображение для этой строки.
         */
        protected void done() {
            // Проходим по массиву, рисуя пиксели, которые были вычислены в doInBackground ().
            // Перерисуем измененную строку.
            for (int i = 0; i < computedRGBValues.length; i++) {
                display.drawPixel(i, yCoordinate, computedRGBValues[i]);
            }
            display.repaint(0, 0, yCoordinate, displaySize, 1);

            // Уменьшить количество оставшихся строк. Если их 0, то вызываем enableUI(true)
            rowsRemaining--;
            if (rowsRemaining == 0) {
                enableUI(true);
            }
        }
    }

    // Точка входа
    public static void main(String[] args)
    {
        FractalExplorer displayExplorer = new FractalExplorer(600);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
}