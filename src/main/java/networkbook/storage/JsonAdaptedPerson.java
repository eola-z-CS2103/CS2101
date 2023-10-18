package networkbook.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import networkbook.commons.exceptions.IllegalValueException;
import networkbook.model.person.Course;
import networkbook.model.person.Email;
import networkbook.model.person.Graduation;
import networkbook.model.person.Link;
import networkbook.model.person.Name;
import networkbook.model.person.Person;
import networkbook.model.person.Phone;
import networkbook.model.person.Priority;
import networkbook.model.person.Specialisation;
import networkbook.model.tag.Tag;
import networkbook.model.util.UniqueList;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final List<JsonAdaptedProperty<Phone>> phones = new ArrayList<>();
    private final List<JsonAdaptedProperty<Email>> emails = new ArrayList<>();
    private final List<JsonAdaptedProperty<Link>> links = new ArrayList<>();
    private final String graduation;
    private final String course;
    private final String specialisation;
    private final List<JsonAdaptedProperty<Tag>> tags = new ArrayList<>();
    private final String priority;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name,
                             @JsonProperty("phones") List<JsonAdaptedProperty<Phone>> phones,
                             @JsonProperty("emails") List<JsonAdaptedProperty<Email>> emails,
                             @JsonProperty("links") List<JsonAdaptedProperty<Link>> links,
                             @JsonProperty("graduation") String graduation,
                             @JsonProperty("course") String course,
                             @JsonProperty("specialisation") String specialisation,
                             @JsonProperty("tags") List<JsonAdaptedProperty<Tag>> tags,
                             @JsonProperty("priority") String priority) {
        this.name = name;
        if (phones != null) {
            this.phones.addAll(phones);
        }
        if (emails != null) {
            this.emails.addAll(emails);
        }
        if (links != null) {
            this.links.addAll(links);
        }
        this.graduation = graduation;
        this.course = course;
        this.specialisation = specialisation;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        this.priority = priority;
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phones.addAll(source.getPhones().stream()
                .map(JsonAdaptedProperty::new)
                .collect(Collectors.toList()));
        emails.addAll(source.getEmails().stream()
                .map(JsonAdaptedProperty::new)
                .collect(Collectors.toList()));
        links.addAll(source.getLinks().stream()
                .map(JsonAdaptedProperty::new)
                .collect(Collectors.toList()));
        graduation = source.getGraduation().map(Graduation::toString).orElse(null);
        course = source.getCourse().map(Course::toString).orElse(null);
        specialisation = source.getSpecialisation().map(Specialisation::toString).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedProperty::new)
                .collect(Collectors.toList()));
        priority = source.getPriority().map(Priority::toString).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (!phones.stream()
                .map(JsonAdaptedProperty::getName)
                .allMatch(Phone::isValidPhone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final UniqueList<Phone> modelPhones = new UniqueList<>();
        phones.forEach(phone -> modelPhones.add(new Phone(phone.getName())));

        if (!emails.stream()
                .map(JsonAdaptedProperty::getName)
                .allMatch(Email::isValidEmail)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final UniqueList<Email> modelEmails = new UniqueList<>();
        emails.forEach(email -> modelEmails.add(new Email(email.getName())));

        if (!links.stream()
                .map(JsonAdaptedProperty::getName)
                .allMatch(Link::isValidLink)) {
            throw new IllegalValueException(Link.MESSAGE_CONSTRAINTS);
        }
        final UniqueList<Link> modelLinks = new UniqueList<>();
        links.forEach(link -> modelLinks.add(new Link(link.getName())));

        Graduation modelGraduation = null;
        if (graduation != null) {
            if (!Graduation.isValidGraduation(graduation)) {
                throw new IllegalValueException(Graduation.MESSAGE_CONSTRAINTS);
            }
            modelGraduation = new Graduation(graduation);
        }

        Course modelCourse = null;
        if (course != null) {
            if (!Course.isValidCourse(course)) {
                throw new IllegalValueException(Course.MESSAGE_CONSTRAINTS);
            }
            modelCourse = new Course(course);
        }

        Specialisation modelSpecialisation = null;
        if (specialisation != null) {
            if (!Specialisation.isValidSpecialisation(specialisation)) {
                throw new IllegalValueException(Specialisation.MESSAGE_CONSTRAINTS);
            }
            modelSpecialisation = new Specialisation(specialisation);
        }

        final UniqueList<Tag> modelTags = new UniqueList<>();
        for (JsonAdaptedProperty<Tag> tag : tags) {
            modelTags.add(tag.toModelType(Tag::isValidTagName, Tag.MESSAGE_CONSTRAINTS, Tag::new));
        }

        Priority modelPriority = null;
        if (priority != null) {
            if (!Priority.isValidPriority(Priority.parsePriorityLevel(priority))) {
                throw new IllegalValueException(Priority.MESSAGE_CONSTRAINTS);
            }
            modelPriority = new Priority(priority);
        }

        return new Person(modelName, modelPhones, modelEmails, modelLinks, modelGraduation, modelCourse,
                modelSpecialisation, modelTags, modelPriority);
    }

}
