package org.echosoft.common.cli.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.echosoft.common.utils.DateUtil;
import org.echosoft.common.utils.StringUtil;

/**
 * Содержит результат разбора аргументов командной строки.
 *
 * @author Anton Sharapov
 */
public class CommandLine implements Serializable {

    /**
     * @return Список поддерживаемых форматов даты и времени.
     */
    public static List<String> getSupportedDatePatterns() {
        return Collections.unmodifiableList(Arrays.asList(DEFAULT_DATE_PATTERNS));
    }

    private static final String[] DEFAULT_DATE_PATTERNS = {"dd.MM.yyyy'T'HH:mm", "dd.MM.yyyy HH:mm", "dd.MM.yyyy", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};

    private final Options options;
    private final List<String> unresolvedArgs;
    private final Map<Option, String> values;
    private final Collection<String> datePatterns;
    private boolean extendedDateFormatAllowed;

    CommandLine(final Options options) {
        this.options = options;
        this.unresolvedArgs = new ArrayList<>();
        this.values = new HashMap<>();
        this.datePatterns = new ArrayList<>(Arrays.asList(DEFAULT_DATE_PATTERNS));
        this.extendedDateFormatAllowed = false;
    }

    /**
     * Если <code>true</code> то сигнализирует о наличии в командной строке нераспознанных в процессе парсинга токенов.
     *
     * @return <code>true</code> при наличии в командной строке нераспознанных токенов.
     */
    public boolean hasUnresolvedArgs() {
        return unresolvedArgs.size() > 0;
    }

    /**
     * Возвращает неразобранную часть аргументов командной строки. Если таковая отсутствует, то метод вернет пустой список.
     *
     * @return часть аргументов командной строки оставшихся неразобранными.
     */
    public String[] getUnresolvedArgs() {
        return unresolvedArgs.toArray(new String[unresolvedArgs.size()]);
    }

    /**
     * Возвращает множество всех опций которые были указаны в аргументах командной строки.
     *
     * @return множество опций которые были указаны в аргументах командной строки.
     */
    public Set<Option> getOptions() {
        return values.keySet();
    }

    /**
     * Определяет была ли указана в аргументах командной строки указанная опция.
     *
     * @param option опция.
     * @return <code>true</code> если указанная опция была указана в аргументах командной строки.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public boolean hasOption(final Option option) throws CLParserException {
        if (!options.hasOption(option))
            throw new UnknownOptionException(option);
        return values.keySet().contains(option);
    }

    /**
     * Возвращает <code>true</code> если опция с указанным именем присутствует в командной строке.
     *
     * @param optionName краткое либо полное название опции.
     * @return <code>true</code> если указанная опция присутствует в командной строке.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public boolean hasOption(final String optionName) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        return values.containsKey(opt);
    }

    /**
     * Возвращает значение опции если она присутствует в разобранной командной строке.
     *
     * @param option       опция чье значение требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public String getOptionValue(final Option option, final String defaultValue) throws CLParserException {
        if (!options.hasOption(option))
            throw new UnknownOptionException(option);
        final String result = values.get(option);
        return result != null ? result : defaultValue;
    }

    /**
     * Возвращает значение опции если она присутствует в разобранной командной строке.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public String getOptionValue(final String optionName, final String defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        return result != null ? result : defaultValue;
    }

    /**
     * Возвращает значение опции в виде целого числа.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в целое число.
     */
    public Integer getOptionIntValue(final String optionName, final Integer defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        if (result != null) {
            try {
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде большого целого числа.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в целое число.
     */
    public Long getOptionLongValue(final String optionName, final Long defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        if (result != null) {
            try {
                return Long.parseLong(result);
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде числа с плавающей запятой.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в число.
     */
    public Double getOptionDoubleValue(final String optionName, final Double defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        if (result != null) {
            try {
                return Double.parseDouble(result);
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде массива целых чисел которые были введены пользователем с использованием запятой в качестве разделителя.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в массив чисел.
     */
    public int[] getOptionIntArrayValue(final String optionName, final int[] defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String[] tokens = splitTokens(values.get(opt));
        if (tokens != null && tokens.length > 0) {
            try {
                final int[] result = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    result[i] = Integer.parseInt(tokens[i]);
                }
                return result;
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде массива длинных целых чисел которые были введены пользователем с использованием запятой в качестве разделителя.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в массив чисел.
     */
    public long[] getOptionLongArrayValue(final String optionName, final long[] defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String[] tokens = splitTokens(values.get(opt));
        if (tokens != null && tokens.length > 0) {
            try {
                final long[] result = new long[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    result[i] = Long.parseLong(tokens[i]);
                }
                return result;
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде даты определенного формата.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки в дату указанного формата
     */
    public Date getOptionDateValue(final String optionName, final Date defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String textValue = values.get(opt);
        if (textValue != null) {
            try {
                Throwable lastCause = null;
                for (String pattern : datePatterns) {
                    final SimpleDateFormat formatter = new SimpleDateFormat(pattern);
                    try {
                        return formatter.parse(textValue);
                    } catch (ParseException e) {
                        lastCause = e;
                    }
                }
                if (extendedDateFormatAllowed) {
                    try {
                        final Date result = DateUtil.calculate(new Date(), textValue);
                        if (result != null)
                            return result;
                    } catch (ParseException e) {
                        lastCause = e;
                    }
                }
                if (lastCause != null)
                    throw new CLParserException(lastCause.getMessage(), lastCause);
            } catch (CLParserException e) {
                throw e;
            } catch (Exception e) {
                throw new CLParserException(e.getMessage(), e);
            }
        }
        return defaultValue;
    }

    public Collection<String> getDatePatterns() {
        return datePatterns;
    }
    public void setDatePatterns(final String... patterns) {
        datePatterns.clear();
        for (String pattern : patterns) {
            pattern = StringUtil.trim(pattern);
            if (pattern != null)
                datePatterns.add(pattern);
        }
        if (datePatterns.isEmpty())
            datePatterns.addAll(Arrays.asList(DEFAULT_DATE_PATTERNS));
    }

    public boolean isExtendedDateFormatAllowed() {
        return extendedDateFormatAllowed;
    }
    public void setExtendedDateFormatAllowed(final boolean extendedDateFormatAllowed) {
        this.extendedDateFormatAllowed = extendedDateFormatAllowed;
    }

    void addUnresolvedArg(final String token) {
        if (token != null && !token.isEmpty())
            unresolvedArgs.add(token);
    }

    void setOptionValue(final Option option, String value) throws CLParserException {
        if (!options.hasOption(option))
            throw new UnknownOptionException(option);
        if (value != null && value.length() > 2 && value.startsWith("@") && value.endsWith("@")) {
            final File file = new File(value.substring(1, value.length() - 1));
            if (!file.isFile())
                throw new CLParserException("File '" + file + "' not found");
            try {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    final StringBuilder buf = new StringBuilder();
                    String line;
                    while( (line = reader.readLine()) != null) {
                        buf.append(line).append('\n');
                    }
                    value = buf.toString();
                }
            } catch (IOException e) {
                throw new CLParserException("Can't read file '" + file + "': " + e.getMessage(), e);
            }
        }
        values.put(option, value);
    }

    void setOption(final Option option) throws CLParserException {
        setOptionValue(option, null);
    }

    private static String[] splitTokens(final String text) {
        if (text == null)
            return null;
        final ArrayList<String> list = new ArrayList<>();
        int start = -1;
        for (int i = 0, len = text.length(); i < len; i++) {
            final char c = text.charAt(i);
            if (c <= ' ' || c == ',' || c == ';') {
                if (start >= 0) {
                    list.add(text.substring(start, i));
                    start = -1;
                }
            } else {
                if (start < 0)
                    start = i;
            }
        }
        if (start >= 0)
            list.add(text.substring(start));
        return list.toArray(new String[list.size()]);
    }
}
